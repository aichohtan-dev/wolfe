package com.wolfe.catalog;

import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;

class PdfBoxCompatibilityTest {
  @Test void pdfBoxCanCreateLoadAndInstantiateStandardFont() throws Exception {
    try (PDDocument doc = new PDDocument()) {
      doc.addPage(new PDPage());
      assertNotNull(new PDType1Font(Standard14Fonts.FontName.HELVETICA));
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      doc.save(out);
      try (PDDocument loaded = Loader.loadPDF(out.toByteArray())) { assertEquals(1, loaded.getNumberOfPages()); }
    }
  }
}
