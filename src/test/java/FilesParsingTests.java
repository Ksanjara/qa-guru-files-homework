import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import model.Structure;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.WorkWithFiles;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class FilesParsingTests {

    WorkWithFiles workWithFiles = new WorkWithFiles();

    @DisplayName("Проверка что zip архив содержит необходимые файлы")
    @Test
    void ZipParsingTest() throws Exception {
        try (ZipInputStream zis = new ZipInputStream(
                getClass().getResourceAsStream(workWithFiles.getZipName())
        )) {
            ZipEntry entry;
            List<String> expectedFiles = List.of("qa_guru/pdf_primer.pdf", "qa_guru/sample1.csv", "qa_guru/sample2.xlsx");
            List<String> actualFiles = new ArrayList<>();

            while ((entry = zis.getNextEntry()) != null) {
                actualFiles.add(entry.getName());
            }
            assertEquals(expectedFiles, actualFiles);
        }
    }

    @DisplayName("Проверка содержимого pdf файла")
    @Test
    void pdfParsingTest() throws Exception {
        try (InputStream pdfFile = workWithFiles.getFileFromZip("pdf_primer.pdf")) {
            PDF pdf = new PDF(pdfFile);
            assertThat(pdf.numberOfPages).isEqualTo(1);
            assertThat(pdf.text.contains("Пример документа в формате PDF"));
        }
    }

    @DisplayName("Проверка содержимого xlsx файла")
    @Test
    void xlsxParsingTest() throws Exception {
        try (InputStream xlsxFile = workWithFiles.getFileFromZip("sample2.xlsx")) {
            XLS xlsFile = new XLS(xlsxFile);
            String actualTitle1 = xlsFile.excel.getSheetAt(0).getRow(1).getCell(0).getStringCellValue();
            assertThat(actualTitle1).isEqualTo("My Title");
        }
    }

    @DisplayName("Проверка содержимого csv файла")
    @Test
    void csvParsingTest() throws Exception {
        try (InputStream csvFile = workWithFiles.getFileFromZip("sample1.csv")) {
            CSVReader csvReader = new CSVReader(new InputStreamReader(csvFile));
            List<String[]> data = csvReader.readAll();
            assertThat(data.size()).isEqualTo(10);
            assertThat(new String[]{"May", "  0.1", "  0", "  0", " 1", " 1", " 0", " 0", " 0", " 2", " 0", "  0", "  0  "})
                    .isEqualTo(data.get(1));
        }
    }

    @DisplayName("Проверка содержимого json файла")
    @Test
    void jsonParsingTest() throws Exception {
        File jsonFile = new File("src/test/resources/example.json");
        ObjectMapper objectMapper = new ObjectMapper();

        Structure actualStructure = objectMapper.readValue(jsonFile, Structure.class);

        assertThat(actualStructure.getHeader()).isEqualTo("SVG Viewer");
        assertThat(actualStructure.getItems().size()).isEqualTo(22);
    }

}



