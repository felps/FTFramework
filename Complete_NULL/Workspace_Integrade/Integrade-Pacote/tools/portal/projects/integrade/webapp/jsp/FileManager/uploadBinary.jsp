<%@ page import="java.util.Vector,java.util.Iterator"%>
<%@ taglib uri="/portletUI" prefix="ui"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ page import="org.integrade.portlets.RepositoryFile"%>
<portlet:defineObjects />
<%int i;%>
<%Vector tree = (Vector) request.getAttribute("repositoryTree");%>
<%RepositoryFile file;%>
<%Iterator it = null;%>
<%if (tree != null) it = tree.iterator();%>
<%String callAction = (String)request.getAttribute("callAction");%>

<ui:panel>
	<ui:fileform action="uploadBinary">
	<ui:table>
	<ui:tablerow>
			<ui:tablecell>
					<ui:text value="File:" />
                </ui:tablecell>
				<ui:tablecell>
					<ui:fileinput beanId="userfile" size="20" maxlength="20" />
				</ui:tablecell>
				<ui:tablecell />
				<ui:tablecell>
					<ui:actionsubmit action="uploadBinary" value="Upload" />
				</ui:tablecell>
				<ui:tablecell />
	</ui:tablerow>
	</ui:table>
	</ui:fileform>
	<ui:group>
			<%if (it != null) {%>
			<%while (it.hasNext()) {%>
			<%file = (RepositoryFile) it.next();%>
			<%i = file.getPath().split("/").length;%>
			<ui:table>
				<ui:tablerow>
					<ui:tablecell width="<%=Integer.toString((i+1)*20) + "px"%>" align="right">
						<%switch (file.getKind().value()) {
						case 0: %>
						<ui:actionlink action="toggleTreeKnot" >
							<ui:actionparam name="target" value="<%=file.getPath()%>"/>
						<%if(file.isOpened()){%>
							<ui:image src="/integrade/images/minus.gif" />
						<%}else{%>
							<ui:image src="/integrade/images/plus.gif" />
						<%}%>
						</ui:actionlink>
						<ui:image src="/integrade/images/appdir_icon.gif" />
						<%break;
						case 1: %>
						<ui:actionlink action="toggleTreeKnot" >
							<ui:actionparam name="target" value="<%=file.getPath()%>"/>
						<%if(file.isOpened()){%>
							<ui:image src="/integrade/images/minus.gif" />
						<%}else{%>
							<ui:image src="/integrade/images/plus.gif" />
						<%}%>
						</ui:actionlink>
						<ui:image src="/integrade/images/folder_icon.gif" />
						<%break;
						case 2: %>
						<ui:image src="/integrade/images/bin_icon.gif" />
						<%break;
						case 3 :%>
						<ui:image src="/integrade/images/desc_icon.gif" />
						<%break;
						default: %>
						<ui:image src="/integrade/images/folder_icon.gif" />
						<%break;
						}%>
					</ui:tablecell>
					<ui:tablecell align="left" cssStyle="<%="background-color: " + ((file.isSelected()) ? "#DDDDDD" : "#EEEEEE")%>">
						<%if (file.isSelected()) {%>
						<ui:text value="<%=file.getName()%>"/>
						<%} else {%>
						<ui:actionlink action="selectItem" value="<%=file.getName()%>">
							<ui:actionparam name="target" value="<%=file.getPath()%>"/>
						</ui:actionlink>
						<%}%>
					</ui:tablecell>
				</ui:tablerow>
			</ui:table>
			<%}
						}%>
		</ui:group>
</ui:panel>
