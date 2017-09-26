<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="promo-box[starts-with(@class,'related-')]">
            <xsl:choose>
                <xsl:when test="promo-headline/descendant::content">
                    <xsl:element name="related">
                        <xsl:attribute name="id">
                            <xsl:value-of select="promo-headline/descendant::content/@id" />
                        </xsl:attribute>
                        <xsl:attribute name="type">
                            <xsl:value-of select="promo-headline/descendant::content/@type" />
                        </xsl:attribute>
                        <xsl:apply-templates mode="related" />
                    </xsl:element>
                </xsl:when>
                <xsl:when test="promo-headline/p/a">
                    <xsl:element name="related">
                        <xsl:attribute name="url">
                            <xsl:value-of select="promo-headline/p/a/@href" />
                        </xsl:attribute>
                        <xsl:apply-templates mode="related" />
                    </xsl:element>
                </xsl:when>
                <xsl:otherwise>
                    <!-- Output Nothing -->
                </xsl:otherwise>
            </xsl:choose>
    </xsl:template>

    <xsl:template match="promo-title" mode="related">
         <title><xsl:value-of select="." /></title>
    </xsl:template>

    <xsl:template match="promo-headline" mode="related">
        <headline><xsl:value-of select="." /></headline>
    </xsl:template>

    <xsl:template match="promo-image" mode="related">
        <media>
            <xsl:apply-templates />
        </media>
    </xsl:template>

    <xsl:template match="promo-intro" mode="related">
        <intro>
            <xsl:apply-templates />
        </intro>
    </xsl:template>


</xsl:stylesheet>