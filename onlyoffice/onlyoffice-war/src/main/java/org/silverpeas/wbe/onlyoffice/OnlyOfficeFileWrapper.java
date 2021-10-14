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

import org.silverpeas.core.wbe.WbeFile;
import org.silverpeas.core.webapi.wbe.WbeFileWrapper;

import static org.silverpeas.core.util.Charsets.UTF_8;
import static org.silverpeas.kernel.util.StringUtil.asBase64;

/**
 * This WRAPPER is used by WBE host and gives the possibility to add functionality
 * contextualized to a WbeFile.
 * @author silveryocha
 */
public class OnlyOfficeFileWrapper extends WbeFileWrapper {

  OnlyOfficeFileWrapper(final WbeFile wbeFile) {
    super(wbeFile);
  }

  /**
   * Gets the document key. As the key MUST change after each save, it is composed with the
   * identifier of the document in Silverpeas and the last save date as base64.
   * @return a document key as string.
   */
  public String key() {
    final String keySuffix = asBase64(lastModificationDate().toString().getBytes(UTF_8));
    return id() + "-" + keySuffix.replace('=', 'k');
  }
}
