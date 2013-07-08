<%@ taglib uri="/portletUI" prefix="ui"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ page
	import="java.util.Vector,
	        java.util.Enumeration,
	        asct.shared.ApplicationState,
	        asct.shared.ExecutionRequestStatus"%>
<portlet:defineObjects />

<%Vector outputFiles = (Vector) request.getAttribute("outputFiles");%>
<%Enumeration jobs = (Enumeration) request.getAttribute("jobs");%>
<%ExecutionRequestStatus status;%>
<%String buff;%>
<%String outputPath = (String) request.getAttribute("outputPath");%>
<%int outputFileNumber = 0;%>
<%int i;%>
<%if (outputFiles != null) {
	outputFileNumber = outputFiles.size();
			}%>
<%String appType = (String) request.getAttribute("appType");%>
<%Vector inputFileNames = (Vector) request.getAttribute("inputFiles");%>
<%Vector parametricCopies = (Vector) request
					.getAttribute("parametricCopies");%>
<%int parNumber = 0;%>
<%if (parametricCopies != null) {
				parNumber = parametricCopies.size();
			}%>

<ui:panel>
	<ui:table>
		<ui:tablerow>
			<ui:tablecell>
				<ui:group>
					<ui:table>
						<ui:form>
							<ui:tablerow>
								<ui:tablecell>
									<ui:listbox beanId="applications" />
								</ui:tablecell>
								<ui:tablecell>
									<%if (appType.compareTo("binary") == 0) {%>
									<ui:text value="Sequential" />
									<%} else {%>
									<ui:actionlink action="setAppType" value="Sequential">
										<ui:actionparam name="appType" value="binary" />
									</ui:actionlink>
									<%}%>
									<%if (appType.compareTo("bsp") == 0) {%>
									<ui:text value="BSP" />
									<%} else {%>
									<ui:actionlink action="setAppType" value="BSP">
										<ui:actionparam name="appType" value="bsp" />
									</ui:actionlink>
									<%}%>
									<%if (appType.compareTo("mpi") == 0) {%>
									<ui:text value="MPI" />
									<%} else {%>
									<ui:actionlink action="setAppType" value="MPI">
										<ui:actionparam name="appType" value="mpi" />
									</ui:actionlink>
									<%}%>
									<%if(appType.compareTo("parametric")==0) {%>
									<ui:text value="Parametric" />
									<%} else {%>
									<ui:actionlink action="setAppType" value="Parametric">
										<ui:actionparam name="appType" value="parametric" />
									</ui:actionlink>
									<%}%>
								</ui:tablecell>
							</ui:tablerow>
							<ui:tablerow>
								<ui:tablecell>
									<ui:text value="Preferences: " />
								</ui:tablecell>
								<ui:tablecell>
									<ui:textfield beanId="preferences" size="50" />
								</ui:tablecell>
							</ui:tablerow>
							<ui:tablerow>
								<ui:tablecell>
									<ui:text value="Constraints: " />
								</ui:tablecell>
								<ui:tablecell>
									<ui:textfield beanId="constraints" size="50" />
								</ui:tablecell>
							</ui:tablerow>
							<ui:tablerow>
								<ui:tablecell>
									<ui:text value="Arguments: " />
								</ui:tablecell>
								<ui:tablecell>
									<ui:textfield beanId="arguments" size="50" />
								</ui:tablecell>
							</ui:tablerow>
							<%if (appType.compareTo("bsp") == 0) {%>
							<ui:tablerow>
								<ui:tablecell>
									<ui:text value="Number of tasks: " />
								</ui:tablecell>
								<ui:tablecell>
									<ui:textfield beanId="numberOfTasks" size="50" />
								</ui:tablecell>
							</ui:tablerow>
							<%}%>
							<%if (appType.compareTo("mpi") == 0) {%>
							<ui:tablerow>
								<ui:tablecell>
									<ui:text value="Number of tasks: " />
								</ui:tablecell>
								<ui:tablecell>
									<ui:textfield beanId="numberOfTasks" size="50" />
								</ui:tablecell>
							</ui:tablerow>
							<%}%>
							<ui:tablerow>
								<ui:tablecell valign="top">
									<ui:table>
										<ui:tablerow>
											<ui:tablecell>
												<ui:text value="Output Files" />
											</ui:tablecell>
											<ui:tablecell />
										</ui:tablerow>
										<%for (i = 0; i < outputFileNumber; i++) {%>
										<ui:tablerow>
											<ui:tablecell>
												<ui:text value="<%=(String)outputFiles.get(i)%>" />
											</ui:tablecell>
											<ui:tablecell align="right">
												<ui:actionsubmit action="removeOutput" value="-">
													<ui:actionparam name="target"
														value="<%= Integer.toString(i) %>" />
												</ui:actionsubmit>
											</ui:tablecell>
										</ui:tablerow>
										<%}%>
										<ui:tablerow>
											<ui:tablecell>
												<ui:textfield beanId="outputFileName" size="20" />
											</ui:tablecell>
											<ui:tablecell>
												<ui:actionsubmit action="addOutput" value="+" />
											</ui:tablecell>
										</ui:tablerow>
									</ui:table>
								</ui:tablecell>
								<ui:tablecell valign="top">
									<ui:panel>
										<ui:tablerow>
											<ui:tablecell>
												<ui:text value="Input Files" />
											</ui:tablecell>
											<ui:tablecell/>
										</ui:tablerow>
										<%if (inputFileNames.size() == 0) {%>
										<ui:tablerow>
											<ui:tablecell>
												<ui:text value="No available input files" />
											</ui:tablecell>
										</ui:tablerow>
										<%} else {%>
										<ui:tablerow>
											<ui:tablecell>
												<ui:text value="Selected" />
											</ui:tablecell>
											<ui:tablecell>
												<ui:text value="Available files" />
											</ui:tablecell>
										</ui:tablerow>										
										<%for (i = 0; i < inputFileNames.size(); i++) {%>
										<%buff = (String)inputFileNames.get(i); %>
										<%String color =  (buff.charAt(0)=='T') ? "#DDDDDD" : "#EEEEEE";%>
										<ui:tablerow>
											<ui:tablecell> <%=(buff.charAt(0)=='T') ? "yes" : "no" %> </ui:tablecell>
											<ui:tablecell cssStyle="<%="background-color: " + color %>">
												<ui:actionlink action="toggleInputFile"
													value="<%=buff.substring(1) %>">
													<ui:actionparam name="target" value="<%=buff %>" />
												</ui:actionlink>
											</ui:tablecell>
											<ui:tablecell>
												<ui:actionlink action="deleteInputFile"	value="-">
													<ui:actionparam name="target" value="<%=buff %>" />
												</ui:actionlink>
											</ui:tablecell>
										</ui:tablerow>
										<%}%>
										<%}%>
									</ui:panel>
								</ui:tablecell>
								<ui:tablecell/>
							</ui:tablerow>
							<%if (appType.compareTo("parametric") == 0) {%>
							<ui:tablerow>
								<ui:tablecell>
									<ui:textfield beanId="parametricCopyName" />
								</ui:tablecell>
								<ui:tablecell>
									<ui:actionsubmit action="addParametricCopy"
										value="Add Parametric Copy" />
								</ui:tablecell>
							</ui:tablerow>
							<%for (i = 0; i < parNumber; i++) {%>
							<ui:tablerow>
								<ui:tablecell>
									<ui:text value="<%= (String) parametricCopies.get(i) %>" />
								</ui:tablecell>
								<ui:tablecell>
									<ui:actionsubmit action="removeParametricCopy"
										value="Delete Copy">
										<ui:actionparam name="target"
											value="<%= Integer.toString(i) %>" />
									</ui:actionsubmit>
								</ui:tablecell>
							</ui:tablerow>
							<%}%>
							<%}%>
							<ui:tablerow>
								<ui:tablecell>
									<ui:actionsubmit action="submit" value="Submit Execution" />
								</ui:tablecell>
							</ui:tablerow>
						</ui:form>
					</ui:table>
				</ui:group>
				<ui:group>
					<ui:table>
						<ui:tablerow>
							<ui:tablecell>
								<ui:text value="Uploaded files will be available on the input files list" />
							</ui:tablecell>
						</ui:tablerow>
						<ui:tablerow>
							<ui:fileform action="uploadInputFile">
								<ui:tablecell align="left">
									<ui:text value="File: " />
								</ui:tablecell>
								<ui:tablecell align="left">
									<ui:fileinput beanId="userfile" size="20" maxlength="20" />
								</ui:tablecell>
								<ui:tablecell align="right">
									<ui:actionsubmit action="uploadInputFile" value="Upload" />
								</ui:tablecell>
								<ui:tablecell />
							</ui:fileform>
						</ui:tablerow>
					</ui:table>
				</ui:group>
			</ui:tablecell>
			<ui:tablecell valign="top">
				<ui:group>
					<ui:table>
						<ui:tablerow>
							<ui:tablecell cssStyle="background-color: #EEEEEE">
								Application
							</ui:tablecell>
							<ui:tablecell cssStyle="background-color: #EEEEEE">
								Request Id
							</ui:tablecell>
							<ui:tablecell cssStyle="background-color: #EEEEEE">
								Execution state
							</ui:tablecell>
						</ui:tablerow>
						<%i = 0;%>
						<%while (jobs.hasMoreElements()) {%>
						<%status = (ExecutionRequestStatus) jobs.nextElement();%>
						<%buff = "background-color: " + ((i++ % 2 == 0) ? "#CCCCCC" : "#DDDDDD");%>
						<ui:tablerow>
							<ui:tablecell cssStyle="<%=buff%>">
								<%=status.getApplicationName()%>
							</ui:tablecell>
							<ui:tablecell cssStyle="<%=buff%>">
								<%=status.getRequestId()%>
							</ui:tablecell>
							<ui:tablecell cssStyle="<%=buff%>">
								<%=status.getApplicationState().toString()%>
								<%if(status.getApplicationState().equals(ApplicationState.FINISHED)) {%>
								<a href="<%=outputPath + status.getRequestId() + ".tar.gz"%>">(results)</a>
								<%}%>
							</ui:tablecell>
						</ui:tablerow>
						<%}%>
					</ui:table>
				</ui:group>
			</ui:tablecell>
			<ui:tablecell />
		</ui:tablerow>
	</ui:table>
</ui:panel>

