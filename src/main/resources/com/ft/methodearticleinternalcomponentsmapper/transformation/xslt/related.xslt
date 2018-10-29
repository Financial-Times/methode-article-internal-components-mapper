<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="promo-box[starts-with(@class,'related-')]">
            <xsl:choose>
                <xsl:when test="promo-headline/descendant::ft-content">
                    <xsl:element name="ft-related">
                        <xsl:attribute name="id">
                            <xsl:value-of select="promo-headline/descendant::ft-content/@id" />
                        </xsl:attribute>
                        <xsl:attribute name="type">
                            <xsl:value-of select="promo-headline/descendant::ft-content/@type" />
                        </xsl:attribute>
                        <xsl:apply-templates mode="ft-related" />
                    </xsl:element>
                </xsl:when>
                <xsl:when test="promo-headline/p/a">
                    <xsl:element name="ft-related">
                        <xsl:attribute name="url">
                            <xsl:value-of select="promo-headline/p/a/@href" />
                        </xsl:attribute>
                        <xsl:apply-templates mode="ft-related" />
                    </xsl:element>
                </xsl:when>
                <xsl:otherwise>
                    <!-- Output Nothing -->
                </xsl:otherwise>
            </xsl:choose>
    </xsl:template>

    <xsl:template match="promo-title" mode="ft-related">
         <title><xsl:value-of select="." /></title>
    </xsl:template>

    <xsl:template match="promo-headline" mode="ft-related">
        <headline><xsl:value-of select="." /></headline>
    </xsl:template>

    <xsl:template match="promo-image" mode="ft-related">
        <media>
            <xsl:apply-templates />
        </media>
    </xsl:template>

    <xsl:template match="promo-intro" mode="ft-related">
        <intro>
            <xsl:apply-templates />
        </intro>
    </xsl:template>


</xsl:stylesheet>