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

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.wbe.WbeClientManager;
import org.silverpeas.core.wbe.WbeFile;
import org.silverpeas.core.wbe.WbeHostManager;
import org.silverpeas.core.wbe.WbeUser;
import org.silverpeas.wbe.onlyoffice.util.OnlyOfficeSettings;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.text.MessageFormat.format;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.silverpeas.core.wbe.WbeLogger.logger;

/**
 * @author silveryocha
 */
@Service
public class OnlyOfficeClientManager implements WbeClientManager {

  private final Map<String, String> documentTypeByExtension = new ConcurrentHashMap<>();

  @Override
  public String getName(final String language) {
    return "OnlyOffice";
  }

  @Override
  public boolean isEnabled() {
    return OnlyOfficeSettings.isEnabled();
  }

  @Override
  public boolean isHandled(final WbeFile file) {
    return getDocumentTypeFor(file).isPresent();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Optional<OnlyOfficeEdition> prepareEditionWith(final WbeUser user, final WbeFile file) {
    return getDocumentTypeFor(file).map(t -> new OnlyOfficeEdition(file, user, t));
  }

  @Override
  public Optional<String> getAdministrationUrl() {
    return empty();
  }

  @Override
  public void clear() {
    documentTypeByExtension.clear();
  }

  private synchronized void discover() {
    if (documentTypeByExtension.isEmpty()) {
      WbeHostManager.get().clear();
      logger().debug(() -> "loading handled OnlyOffice extensions");
      List.of("doc", "docm", "docx", "dot", "dotm", "dotx", "epub", "fodt", "htm", "html",
          "mht", "odt", "ott", "pdf", "rtf", "txt", "djvu", "xps")
          .forEach(e -> documentTypeByExtension.put(e, "word"));
      List.of("csv", "fods", "ods", "ots", "xls", "xlsm", "xlsx", "xlt", "xltm", "xltx")
          .forEach(e -> documentTypeByExtension.put(e, "cell"));
      List.of("fodp", "odp", "otp", "pot", "potm", "potx", "pps", "ppsm", "ppsx", "ppt",
          "pptm", "pptx")
          .forEach(e -> documentTypeByExtension.put(e, "slide"));
    }
  }

  private Optional<String> getDocumentTypeFor(final WbeFile file) {
    if (isEnabled()) {
      discover();
      return ofNullable(documentTypeByExtension.get(file.ext()));
    } else if (!documentTypeByExtension.isEmpty()){
      WbeHostManager.get().clear();
      logger().debug(() -> format("removing all handled extensions because of OnlyOffice disabling"));
    }
    return empty();
  }
}
