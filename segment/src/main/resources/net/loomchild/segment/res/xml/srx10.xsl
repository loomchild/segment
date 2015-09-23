<!-- 
XSL stylesheet to convert SRX 1.0 file to SRX 2.0 file.

Parameters:
	maprulename - name of maprule to preserve, as in SRX 2.0 there are no 
			map rules; by default select first map rule in SRX 1.0 file.

(C) Jarek Lipski 2008
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<!-- xalan:indent-amount parameter is needed because default Java indent amount is 0. -->
	<xsl:output method="xml" indent="yes" xmlns:xalan="http://xml.apache.org/xslt" xalan:indent-amount="4"/>
	
	<xsl:param name="maprulename"/>

	<xsl:template match="/srx">
		
		<srx version="2.0" xmlns="http://www.lisa.org/srx20" xsi:schemaLocation="http://www.lisa.org/srx20 srx20.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

			<xsl:element name="header">

				<xsl:attribute name="segmentsubflows">
					<xsl:value-of select="header/@segmentsubflows"/>
				</xsl:attribute>
				<xsl:attribute name="cascade">
					<xsl:value-of select="'no'"/>
				</xsl:attribute>

				<xsl:for-each select="header/formathandle">
					<xsl:element name="formathandle">
						<xsl:attribute name="type">
							<xsl:value-of select="@type"/>
						</xsl:attribute>
						<xsl:attribute name="include">
							<xsl:value-of select="@include"/>
						</xsl:attribute>
					</xsl:element>					
				</xsl:for-each> 

			</xsl:element>

			<body>
			
				<languagerules>
			
					<xsl:for-each select="body/languagerules/languagerule">
		
						<xsl:element name="languagerule">
						
							<xsl:attribute name="languagerulename">
								<xsl:value-of select="@languagerulename"/>
							</xsl:attribute>
		
							<xsl:for-each select="rule">
							
								<xsl:element name="rule">
								
									<xsl:attribute name="break">
										<xsl:value-of select="@break"/>
									</xsl:attribute>
		
									<xsl:if test="string-length(beforebreak/text()) > 0">							
										<xsl:element name="beforebreak">
											<xsl:copy-of select="beforebreak/text()"/>
										</xsl:element>
									</xsl:if>					
		
									<xsl:if test="string-length(afterbreak/text()) > 0">							
										<xsl:element name="afterbreak">
											<xsl:copy-of select="afterbreak/text()"/>
										</xsl:element>
									</xsl:if>					
								
								</xsl:element>		
							
							</xsl:for-each>
			
						</xsl:element>
					
					</xsl:for-each>			

				</languagerules>

				<maprules>

					<!-- 
					Stupid repetition but I couldn't find a method to generalize.
					Setting maprule or position variable does not work and
					using sub template and apply-templates removes namespace
					from language rules in Java.
					 -->

					<xsl:choose>

						<xsl:when test="$maprulename">
						
							<xsl:if test="not(boolean(body/maprules/maprule[@maprulename=$maprulename]))">
								<xsl:message terminate="yes">Map rule "<xsl:value-of select="$maprulename"/>" not found.</xsl:message>
							</xsl:if>

							<xsl:for-each select="body/maprules/maprule[@maprulename=$maprulename]/languagemap">
							
								<xsl:element name="languagemap">
					
									<xsl:attribute name="languagepattern">
										<xsl:value-of select="@languagepattern"/>								
									</xsl:attribute>
									<xsl:attribute name="languagerulename">
										<xsl:value-of select="@languagerulename"/>								
									</xsl:attribute>
								
								</xsl:element>
							
							</xsl:for-each>

						</xsl:when>

						<xsl:otherwise>

							<xsl:for-each select="body/maprules/maprule[position()=1]/languagemap">
							
								<xsl:element name="languagemap">
					
									<xsl:attribute name="languagepattern">
										<xsl:value-of select="@languagepattern"/>								
									</xsl:attribute>
									<xsl:attribute name="languagerulename">
										<xsl:value-of select="@languagerulename"/>								
									</xsl:attribute>
								
								</xsl:element>
							
							</xsl:for-each>

						</xsl:otherwise>

					</xsl:choose>
					
				</maprules>
			
			</body>

		</srx>
	</xsl:template>
	
</xsl:stylesheet>
