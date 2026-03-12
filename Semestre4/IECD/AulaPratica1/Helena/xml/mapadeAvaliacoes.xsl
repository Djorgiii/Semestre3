<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" encoding="UTF-8" indent="yes"/>

    <xsl:template match="/">
        <html>
            <head>
                <title>Calendário de Avaliações - <xsl:value-of select="calendarioAvaliacoes/@curso"/></title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; background-color: #f4f4f9; }
                    h2 { color: #333; }
                    table { width: 100%; border-collapse: collapse; margin-bottom: 20px; background-color: #fff; }
                    th, td { border: 1px solid #ccc; padding: 12px; text-align: left; }
                    th { background-color: #0056b3; color: white; }
                    tr:nth-child(even) { background-color: #f2f2f2; }
                    .header-info { margin-bottom: 20px; font-size: 1.1em; }
                </style>
            </head>
            <body>
                <h2>Calendário de Avaliações</h2>
                <div class="header-info">
                    <strong>Curso:</strong> <xsl:value-of select="calendarioAvaliacoes/@curso"/><br/>
                    <strong>Ano Letivo:</strong> <xsl:value-of select="calendarioAvaliacoes/@anoLetivo"/><br/>
                    <strong>Semestre:</strong> <xsl:value-of select="calendarioAvaliacoes/semestre/@numero"/>
                </div>

                <table>
                    <thead>
                        <tr>
                            <th>Disciplina</th>
                            <th>Tipo de Avaliação</th>
                            <th>Data</th>
                            <th>Hora</th>
                            <th>Duração</th>
                        </tr>
                    </thead>
                    <tbody>
                        <xsl:for-each select="calendarioAvaliacoes/semestre/disciplina">
                            <xsl:for-each select="avaliacao">
                                <tr>
                                    <td>
                                        <xsl:if test="position() = 1">
                                            <strong><xsl:value-of select="../@nome"/></strong>
                                        </xsl:if>
                                    </td>
                                    <td><xsl:value-of select="@tipo"/></td>
                                    <td><xsl:value-of select="data"/></td>
                                    <td><xsl:value-of select="hora"/></td>
                                    <td><xsl:value-of select="duração"/></td>
                                </tr>
                            </xsl:for-each>
                        </xsl:for-each>
                    </tbody>
                </table>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>