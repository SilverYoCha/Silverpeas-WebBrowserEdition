<%--
  ~ Copyright (C) 2000 - 2019 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>
<%@ page import="org.silverpeas.core.util.URLUtil" %>
<%@ page import="org.silverpeas.core.admin.user.model.User" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>

<c:set var="currentUser" value="<%=User.getCurrentRequester()%>"/>
<jsp:useBean id="currentUser" type="org.silverpeas.core.admin.user.model.User"/>
<c:set var="wbeEdition" value="${requestScope.WbeEdition}"/>
<jsp:useBean id="wbeEdition" type="org.silverpeas.wbe.onlyoffice.OnlyOfficeEdition"/>
<c:set var="wbeUser" value="${wbeEdition.user}"/>
<c:set var="wbeFile" value="${wbeEdition.file}"/>
<c:set var="ext" value="${wbeFile.ext()}"/>
<c:set var="serverUrl" value="<%=URLUtil.getCurrentServerURL()%>"/>
<c:url var="sourceUrl" value="/services/wbe/oo/files/${wbeFile.id()}/contents">
  <c:param name="access_token" value="${wbeUser.accessToken}"/>
</c:url>
<c:url var="callbackUrl" value="/services/wbe/oo/files/${wbeFile.id()}/callback">
  <c:param name="access_token" value="${wbeUser.accessToken}"/>
</c:url>

<view:sp-page>
  <view:sp-head-part minimalSilverpeasScriptEnv="true">
    <meta charset="utf-8">
    <%-- Enable IE Standards mode --%>
    <meta http-equiv="x-ua-compatible" content="ie=edge">
    <meta name="viewport"
          content="width=device-width, initial-scale=1, maximum-scale=1, minimum-scale=1, user-scalable=no">

    <style type="text/css">
      html, body {
        height: 100%;
        margin: 0;
        padding: 0;
        overflow: hidden;
        -ms-content-zooming: none;
      }
    </style>
  </view:sp-head-part>
  <view:sp-body-part>
    <div id="placeholder" style="height: 100%"></div>
    <script type="text/javascript" src="${serverUrl}/wbe/oo/web-apps/apps/api/documents/api.js"></script>
    <script type="text/javascript">
      let config = {
        "document" : {
          "fileType" : "${wbeFile.ext().toLowerCase()}",
          "key" : "${wbeEdition.documentKey}",
          "title" : "${silfn:escapeJs(wbeFile.name())}",
          "url" : "${serverUrl}${sourceUrl}"
        },
        "documentType" : "${wbeEdition.documentType}",
        "editorConfig" : {
          "callbackUrl" : "${serverUrl}${callbackUrl}",
          "user" : {
            "id" : "${wbeUser.id}",
            "name" : "${silfn:escapeJs(wbeUser.user.displayedName)}",
          },
          "lang" : "${currentUser.userPreferences.language}",
          "customization" : {
            "forceSave" : true
          }
        },
        "height" : "100%",
        "width" : "100%"
      };
      window.docEditor = new DocsAPI.DocEditor("placeholder", config);
    </script>
  </view:sp-body-part>
</view:sp-page>
