<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" encoding="UTF-8" indent="yes"/>

    <xsl:variable name="dadosHorario" select="document('../xml/HorarioSemanal.xml')"/>

    <xsl:template match="/">
        <xsl:variable name="turmaAlvo" select="/agenda/inscricoes/inscricao/@turma"/>
        
        <html>
            <head>
                <title>Dashboard Agenda Pessoal</title>
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f7f6; margin: 0; padding: 20px; color: #333; }
                    .container { max-width: 1200px; margin: auto; }
                    
                    .header-main { background: #1a73e8; color: white; padding: 25px; border-radius: 10px; margin-bottom: 30px; }
                    .section-card { background: white; border-radius: 10px; padding: 20px; margin-bottom: 25px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
                    
                    h2 { color: #1a73e8; border-bottom: 2px solid #e8f0fe; padding-bottom: 10px; margin-top: 0; }
                    
                    .item-lista { padding: 10px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
                    .badge-urgente { background: #fce8e6; color: #d93025; padding: 3px 8px; border-radius: 4px; font-size: 0.75em; font-weight: bold; }

                    /* Grid do Horário */
                    .semana-grid { display: flex; gap: 15px; margin-top: 20px; }
                    .dia-col { flex: 1; min-width: 200px; background: #ebf1f7; border-radius: 8px; padding: 12px; }
                    .dia-header { background: #2c3e50; color: white; text-align: center; padding: 10px; border-radius: 6px; font-weight: bold; text-transform: uppercase; margin-bottom: 15px; }
                    
                    .aula-card { background: white; border-radius: 8px; padding: 12px; margin-bottom: 10px; border-left: 5px solid #fbbc04; box-shadow: 0 2px 5px rgba(0,0,0,0.08); }
                    .aula-uc { font-weight: bold; color: #202124; display: block; }
                    .aula-time { color: #1a73e8; font-size: 0.85em; font-weight: bold; margin: 4px 0; }
                    .aula-sala { font-weight: bold; color: #e67e22; font-size: 0.85em; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header-main">
                        <h1>Minha Agenda Pessoal</h1>
                        <p>Inscrito na Turma: <strong><xsl:value-of select="$turmaAlvo"/></strong></p>
                    </div>

                    <div class="section-card">
                        <h2>📌 Compromissos Próximos</h2>
                        <xsl:for-each select="/agenda/eventos/evento">
                            <div class="item-lista">
                                <div>📌 <strong><xsl:value-of select="titulo"/></strong> - <xsl:value-of select="local"/></div>
                                <div style="color: #1a73e8; font-weight: bold;"><xsl:value-of select="substring-after(inicio, 'T')"/></div>
                            </div>
                        </xsl:for-each>
                    </div>

                    <div class="section-card">
                        <h2>✅ Lista de Afazeres</h2>
                        <xsl:for-each select="/agenda/tarefas/tarefa">
                            <div class="item-lista">
                                <div>
                                    <strong><xsl:value-of select="descricao"/></strong>
                                    <br/><small style="color: #666;">Prazo: <xsl:value-of select="prazo"/></small>
                                </div>
                                <xsl:if test="prioridade='alta'">
                                    <span class="badge-urgente">URGENTE</span>
                                </xsl:if>
                            </div>
                        </xsl:for-each>
                    </div>

                    <h2>📅 Horário Semanal de Aulas</h2>
                    <div class="semana-grid">
                        <xsl:for-each select="$dadosHorario/agenda/semana/turma[@nome=$turmaAlvo]/dia">
                            <div class="dia-col">
                                <div class="dia-header"><xsl:value-of select="@nome"/></div>
                                
                                <xsl:for-each select="aula">
                                    <div class="aula-card">
                                        <span class="aula-uc"><xsl:value-of select="uc"/></span>
                                        <div class="aula-time">🕒 <xsl:value-of select="inicio"/> - <xsl:value-of select="fim"/></div>
                                        <div class="aula-sala">📍 <xsl:value-of select="sala"/></div>
                                    </div>
                                </xsl:for-each>
                            </div>
                        </xsl:for-each>
                    </div>
                </div>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>