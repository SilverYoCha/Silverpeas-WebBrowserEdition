/*
 * Copyright (C) 2000 - 2021 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.wbe.onlyoffice;

import org.silverpeas.core.SilverpeasExceptionMessages.LightExceptionMessage;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.wbe.WbeFile;
import org.silverpeas.core.web.rs.annotation.Authenticated;
import org.silverpeas.core.webapi.wbe.AbstractWbeFileResource;
import org.silverpeas.core.webapi.wbe.WbeFileEditionContext;
import org.silverpeas.core.webapi.wbe.WbeFileWrapper;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.Set;

import static java.net.http.HttpResponse.BodyHandlers.ofInputStream;
import static java.text.MessageFormat.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.silverpeas.core.util.HttpUtil.httpClientTrustingAnySslContext;
import static org.silverpeas.core.util.HttpUtil.toUrl;
import static org.silverpeas.core.wbe.WbeLogger.logger;

/**
 * @author silveryocha
 */
@WebService
@Path("wbe/oo/files/{fileId}")
@Authenticated
public class OnlyOfficeFileResource extends AbstractWbeFileResource {

  private static final int DOC_IS_BEING_EDITED_STATUS = 1;
  private static final int DOC_IS_READY_FOR_SAVING = 2;
  private static final int DOC_IS_CLOSED_WITH_NO_CHANGES = 4;

  @Path("callback")
  @POST
  public Response sendFileData(InputStream inputStream) {
    return process(() -> {
      final WbeFileEditionContext context = getEditionContext();
      final OnlyOfficeFileWrapper file = (OnlyOfficeFileWrapper) context.getFile();
      try (JsonReader jsonReader = Json.createReader(inputStream)) {
        final JsonObject data = jsonReader.readObject();
        final String documentKey = data.getString("key");
        if (!file.key().equals(documentKey)) {
          return Response.status(CONFLICT).build();
        }
        final int status = data.getInt("status");
        if (status == DOC_IS_READY_FOR_SAVING) {
          saveFile(data.getString("url"), file);
        } else if (status == DOC_IS_BEING_EDITED_STATUS ||
                   status == DOC_IS_CLOSED_WITH_NO_CHANGES) {
          updateEditionUsers(file, data);
        }
      } catch (Exception e) {
        logger().error(new LightExceptionMessage(this, e).singleLineWith("OnlyOffice callback error"));
      }
      final String response = JSONCodec.encodeObject(o -> o.put("error", 0));
      return Response.ok().type(MediaType.APPLICATION_JSON).entity(response).build();
    });
  }

  @Override
  protected WbeFileWrapper wrapWbeFile(final WbeFile file) {
    return new OnlyOfficeFileWrapper(file);
  }

  private void saveFile(final String sourceUrl, final WbeFile file) {
    try {
      logger().debug(() -> format("Saving {0} from OnlyOffice URL {1}", file, sourceUrl));
      final HttpResponse<InputStream> response = httpClientTrustingAnySslContext().send(toUrl(sourceUrl)
          .header("Accept", APPLICATION_OCTET_STREAM)
          .build(), ofInputStream());
      if (response.statusCode() != OK.getStatusCode()) {
        throw new WebApplicationException(response.statusCode());
      }
      try (final InputStream body = response.body()) {
        file.updateFrom(body);
      }
    } catch (Exception e) {
      throw new WebApplicationException(e, Response.Status.NOT_FOUND);
    }
  }

  @GET
  @Path("contents")
  public Response sendFileContentData() {
    return process(() -> {
      final WbeFileEditionContext context = getEditionContext();
      final StreamingOutput streamingOutput = o -> context.getFile().loadInto(o);
      return Response.ok(streamingOutput, APPLICATION_OCTET_STREAM).build();
    });
  }

  private void updateEditionUsers(final WbeFile file, final JsonObject data) {
    final Set<String> userIds = ofNullable(data.getJsonArray("users"))
        .map(a -> a.getValuesAs(JsonString.class).stream()
            .map(JsonString::getString).collect(toSet()))
        .orElse(Collections.emptySet());
    getHostManager().notifyEditionWith(file, userIds);
  }
}
