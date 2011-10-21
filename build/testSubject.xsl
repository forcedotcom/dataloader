<xsl:stylesheet version='1.0'
     xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
<xsl:output method="text"/>
    <xsl:template match="testsuites">
        <xsl:text>dataloader tests - completed with </xsl:text>
        <xsl:value-of select="sum(testsuite/@failures)"/>
        <xsl:text> failures and </xsl:text>
        <xsl:value-of select="sum(testsuite/@errors)"/>
        <xsl:text> errors out of </xsl:text>
        <xsl:value-of select="sum(testsuite/@tests)"/>
        <xsl:text> tests</xsl:text>
    </xsl:template>
</xsl:stylesheet>
