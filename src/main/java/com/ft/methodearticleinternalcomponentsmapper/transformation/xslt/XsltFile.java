package com.ft.methodearticleinternalcomponentsmapper.transformation.xslt;

import com.google.common.base.Preconditions;

public class XsltFile {

    private String name;
    private String content;

    /**
     * Implements the <a href="http://en.wikipedia.org/wiki/Identity_transform">identity transform</a>
     */
    public static XsltFile IDENTITY_TRANSFORM = new XsltFile("identity", "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n" +
            "  <xsl:template match=\"@*|node()\">\n" +
            "    <xsl:copy>\n" +
            "      <xsl:apply-templates select=\"@*|node()\"/>\n" +
            "    </xsl:copy>\n" +
            "  </xsl:template>\n" +
            "</xsl:stylesheet>");


    public XsltFile(String name, String content) {

        Preconditions.checkNotNull(content);
        Preconditions.checkNotNull(name);

        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }
}
