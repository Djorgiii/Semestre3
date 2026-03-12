package Java;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;

public class webdoc {

    public static void main(String[] args) {
        try {
            // 1. Caminhos para os ficheiros (ajusta se necessário)
            String xmlPath = "webapp/xml/AgendaPessoal.xml";
            String xslPath = "webapp/xsl/AgendaPessoal.xsl";
            String outPath = "webapp/html/AgendaFinal.html";

            // 2. Criar as fontes de leitura
            Source xmlFile = new StreamSource(new File(xmlPath));
            Source xslFile = new StreamSource(new File(xslPath));

            // 3. Configurar o destino (o ficheiro HTML de saída)
            Result result = new StreamResult(new File(outPath));

            // 4. Criar o transformador
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(xslFile);

            // 5. Executar a transformação
            System.out.println("A transformar XML em HTML...");
            transformer.transform(xmlFile, result);

            System.out.println("Sucesso! O ficheiro foi gerado em: " + outPath);

        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }
}
