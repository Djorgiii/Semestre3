<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:cal="http://isel.pt/calendario"
    exclude-result-prefixes="cal">

    <!-- Output HTML -->
    <xsl:output method="html" encoding="UTF-8" indent="yes"/>

    <!-- Identity / entry template: produce an HTML document -->
    <xsl:template match="/">
        <html>
            <head>
                <meta charset="utf-8"/>
                <title>Calendário Plurianual</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 1rem; }
                    h1,h2,h3 { color: #2a4d69; }
                    .year { border-top: 2px solid #ccc; padding-top: 1rem; margin-top: 1rem; }
                    .grid { display: grid; grid-template-columns: repeat(auto-fit,minmax(260px,1fr)); gap: 1rem; }
                    .card { border: 1px solid #ddd; padding: .6rem; border-radius: 6px; background: #fbfbfb; }
                    .meta { color: #666; font-size: .9rem; }
                </style>
            </head>
            <body>
                <h1>Calendário Plurianual</h1>
                <p class="meta">Gerado: <xsl:value-of select="/cal:calendarioPlurianual/@generated"/></p>

                <!-- Iterate anos sorted by value -->
                <xsl:for-each select="/cal:calendarioPlurianual/cal:ano">
                    <xsl:sort select="number(@value)" data-type="number" order="ascending"/>
                    <div class="year">
                        <h2>Ano <xsl:value-of select="@value"/></h2>

                        <!-- Semestres -->
                        <h3>Semestres</h3>
                        <div class="grid">
                            <xsl:for-each select="cal:semestres/cal:semestre">
                                <div class="card">
                                    <strong><xsl:value-of select="@name"/></strong>
                                    <div class="meta">ID: <xsl:value-of select="@id"/></div>
                                    <ul>
                                        <li>Início: <xsl:value-of select="cal:start"/></li>
                                        <li>Fim: <xsl:value-of select="cal:end"/></li>
                                        <li>Início das aulas: <xsl:value-of select="cal:classesStart"/></li>
                                        <li>Fim das aulas: <xsl:value-of select="cal:classesEnd"/></li>
                                        <li>Época de Exames: <xsl:value-of select="cal:examPeriodStart"/> - <xsl:value-of select="cal:examPeriodEnd"/></li>
                                    </ul>
                                </div>
                            </xsl:for-each>
                        </div>

                        <!-- Feriados -->
                        <h3>Feriados</h3>
                        <div class="grid">
                            <xsl:for-each select="cal:holidays/cal:holiday">
                                <div class="card">
                                    <strong><xsl:value-of select="@name"/></strong>
                                    <div class="meta">ID: <xsl:value-of select="@id"/></div>
                                    <div>
                                        <xsl:choose>
                                            <xsl:when test="@start and @end">
                                                <xsl:value-of select="@start"/> - <xsl:value-of select="@end"/>
                                            </xsl:when>
                                            <xsl:when test="@start">
                                                <xsl:value-of select="@start"/>
                                            </xsl:when>
                                        </xsl:choose>
                                    </div>
                                </div>
                            </xsl:for-each>
                        </div>

                        <!-- Eventos -->
                        <h3>Eventos</h3>
                        <div class="grid">
                            <xsl:for-each select="cal:events/cal:event">
                                <div class="card">
                                    <strong><xsl:value-of select="@name"/></strong>
                                    <div class="meta">ID: <xsl:value-of select="@id"/></div>
                                    <div>
                                        <xsl:choose>
                                            <xsl:when test="@date">
                                                Data: <xsl:value-of select="@date"/>
                                            </xsl:when>
                                            <xsl:when test="@start and @end">
                                                Período: <xsl:value-of select="@start"/> - <xsl:value-of select="@end"/>
                                            </xsl:when>
                                        </xsl:choose>
                                    </div>
                                </div>
                            </xsl:for-each>
                        </div>

                    </div>
                </xsl:for-each>

            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>