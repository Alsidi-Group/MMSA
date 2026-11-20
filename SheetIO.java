package com.mmsa.records;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/** قراءة وكتابة ملفات Excel (XLSX) و CSV — Java خالص بدون مكتبات خارجية */
public class SheetIO {

    // ---------- كتابة XLSX ----------

    public static void writeXlsx(File out, String[] headers, List<String[]> rows) throws IOException {
        ZipOutputStream z = new ZipOutputStream(new FileOutputStream(out));
        put(z, "[Content_Types].xml", CT);
        put(z, "_rels/.rels", RELS);
        put(z, "xl/workbook.xml", WORKBOOK);
        put(z, "xl/_rels/workbook.xml.rels", WBRELS);
        put(z, "xl/styles.xml", STYLES);
        put(z, "xl/worksheets/sheet1.xml", sheetXml(headers, rows));
        z.close();
    }

    private static void put(ZipOutputStream z, String name, String content) throws IOException {
        z.putNextEntry(new ZipEntry(name));
        z.write(content.getBytes(StandardCharsets.UTF_8));
        z.closeEntry();
    }

    private static String esc(String s) {
        if (s == null) return "";
        StringBuilder b = new StringBuilder();
        for (char ch : s.toCharArray()) {
            switch (ch) {
                case '&': b.append("&amp;"); break;
                case '<': b.append("&lt;"); break;
                case '>': b.append("&gt;"); break;
                case '"': b.append("&quot;"); break;
                case '\'': b.append("&apos;"); break;
                default: b.append(ch);
            }
        }
        return b.toString();
    }

    private static String sheetXml(String[] headers, List<String[]> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
        int n = headers.length;
        sb.append("<cols>");
        for (int i = 0; i < n; i++)
            sb.append("<col min=\"").append(i + 1).append("\" max=\"").append(i + 1)
              .append("\" width=\"26\" customWidth=\"1\"/>");
        sb.append("</cols>");
        sb.append("<sheetData>");
        sb.append("<row r=\"1\">");
        for (int i = 0; i < headers.length; i++) cell(sb, 1, i, headers[i], true);
        sb.append("</row>");
        int r = 2;
        for (String[] row : rows) {
            sb.append("<row r=\"").append(r).append("\">");
            for (int i = 0; i < n && i < row.length; i++) cell(sb, r, i, row[i], false);
            sb.append("</row>");
            r++;
        }
        sb.append("</sheetData></worksheet>");
        return sb.toString();
    }

    private static void cell(StringBuilder sb, int r, int col, String v, boolean head) {
        sb.append("<c r=\"").append(colRef(col)).append(r)
          .append("\" t=\"inlineStr\" s=\"").append(head ? "1" : "0")
          .append("\"><is><t xml:space=\"preserve\">").append(esc(v)).append("</t></is></c>");
    }

    private static String colRef(int i) {
        StringBuilder b = new StringBuilder();
        i++;
        while (i > 0) {
            i--;
            b.insert(0, (char) ('A' + i % 26));
            i /= 26;
        }
        return b.toString();
    }

    private static final String CT =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
            + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
            + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
            + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
            + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
            + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
            + "</Types>";

    private static final String RELS =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
            + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
            + "</Relationships>";

    private static final String WORKBOOK =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
            + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
            + "<sheets><sheet name=\"بيانات الموظفين\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>";

    private static final String WBRELS =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
            + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
            + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>"
            + "</Relationships>";

    private static final String STYLES =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
            + "<fonts count=\"2\"><font><sz val=\"11\"/><name val=\"Calibri\"/></font>"
            + "<font><b/><sz val=\"11\"/><color rgb=\"FFFFFFFF\"/><name val=\"Calibri\"/></font></fonts>"
            + "<fills count=\"3\"><fill><patternFill patternType=\"none\"/></fill>"
            + "<fill><patternFill patternType=\"gray125\"/></fill>"
            + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF1F5C3D\"/><bgColor indexed=\"64\"/></patternFill></fill></fills>"
            + "<borders count=\"1\"><border/></borders>"
            + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
            + "<cellXfs count=\"2\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
            + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\"/></cellXfs>"
            + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
            + "</styleSheet>";

    // ---------- قراءة XLSX ----------

    public static List<String[]> readXlsx(InputStream in) throws Exception {
        return readXlsxAll(in).get(0);
    }

    /** كل أوراق ملف XLSX — لاختيار الورقة التي تحمل بيانات الموظفين تلقائياً */
    public static List<List<String[]>> readXlsxAll(InputStream in) throws Exception {
        ZipInputStream z = new ZipInputStream(in);
        List<String> shared = new ArrayList<>();
        List<byte[]> sheets = new ArrayList<>();
        ZipEntry e;
        int entries = 0;
        while ((e = z.getNextEntry()) != null) {
            // حماية من ملفات zip الضخمة (zip bomb): حد للعدد وحجم الفك الكلي
            if (++entries > 200) throw new IOException("ملف غير صالح (عدد مكونات كبير)");
            String n = e.getName();
            if (n.equals("xl/sharedStrings.xml")) shared = parseShared(readBytes(z, 5_000_000L));
            else if (n.startsWith("xl/worksheets/sheet") && n.endsWith(".xml"))
                sheets.add(readBytes(z, 10_000_000L));
        }
        z.close();
        if (sheets.isEmpty()) throw new IOException("الملف لا يحتوي على ورقة بيانات (sheet1)");
        List<List<String[]>> out = new ArrayList<>();
        for (byte[] sb : sheets)
            out.add(parseSheet(new String(sb, StandardCharsets.UTF_8), shared));
        return out;
    }

    /** قراءة دفق مع سقف أقصى للحجم — يرفض ما يتجاوزه (حماية من التضخم) */
    private static byte[] readBytes(InputStream in, long maxBytes) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        long total = 0;
        while ((n = in.read(buf)) > 0) {
            total += n;
            if (total > maxBytes) throw new IOException("ملف غير صالح أو كبير جداً");
            b.write(buf, 0, n);
        }
        return b.toByteArray();
    }

    private static List<String> parseShared(byte[] xml) throws Exception {
        Document doc = DocumentBuilderFactoryHolder.F.newDocumentBuilder()
                .parse(new InputSource(new ByteArrayInputStream(xml)));
        NodeList ts = doc.getElementsByTagName("t");
        List<String> out = new ArrayList<>();
        for (int i = 0; i < ts.getLength(); i++) {
            out.add(ts.item(i).getTextContent());
        }
        return out;
    }

    private static List<String[]> parseSheet(String xml, List<String> shared) throws Exception {
        Document doc = DocumentBuilderFactoryHolder.F.newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));
        NodeList rows = doc.getElementsByTagName("row");
        List<String[]> out = new ArrayList<>();
        for (int i = 0; i < rows.getLength(); i++) {
            Element rowEl = (Element) rows.item(i);
            NodeList cells = rowEl.getElementsByTagName("c");
            HashMap<Integer, String> vals = new HashMap<>();
            int maxCol = -1;
            for (int j = 0; j < cells.getLength(); j++) {
                Element ce = (Element) cells.item(j);
                int col = colIndex(ce.getAttribute("r"));
                if (col < 0) col = j;
                String t = ce.getAttribute("t");
                String v;
                if ("inlineStr".equals(t)) {
                    NodeList is = ce.getElementsByTagName("t");
                    v = is.getLength() > 0 ? is.item(0).getTextContent() : "";
                } else {
                    NodeList vs = ce.getElementsByTagName("v");
                    v = vs.getLength() > 0 ? vs.item(0).getTextContent() : "";
                    if ("s".equals(t)) {
                        try {
                            int idx = Integer.parseInt(v.trim());
                            v = idx >= 0 && idx < shared.size() ? shared.get(idx) : "";
                        } catch (NumberFormatException ex) {
                            v = "";
                        }
                    }
                }
                if (v == null) v = "";
                vals.put(col, v);
                if (col > maxCol) maxCol = col;
            }
            String[] arr = new String[maxCol + 1];
            for (int k = 0; k <= maxCol; k++) arr[k] = vals.getOrDefault(k, "");
            out.add(arr);
        }
        return out;
    }

    private static int colIndex(String ref) {
        if (ref == null || ref.isEmpty()) return -1;
        int col = 0;
        boolean any = false;
        for (char ch : ref.toCharArray()) {
            if (ch >= 'A' && ch <= 'Z') { col = col * 26 + (ch - 'A' + 1); any = true; }
            else if (ch >= 'a' && ch <= 'z') { col = col * 26 + (ch - 'a' + 1); any = true; }
            else if (ch >= '0' && ch <= '9') break;
        }
        return any ? col - 1 : -1;
    }

    private static final class DocumentBuilderFactoryHolder {
        // مصنع XML محصّن: منع XXE (كيانات خارجية/DOCTYPE) عند قراءة ملفات مستوردة
        static final javax.xml.parsers.DocumentBuilderFactory F = secureFactory();

        private static javax.xml.parsers.DocumentBuilderFactory secureFactory() {
            javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            try {
                f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                f.setFeature("http://xml.org/sax/features/external-general-entities", false);
                f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                f.setXIncludeAware(false);
                f.setExpandEntityReferences(false);
            } catch (Exception ignored) {}
            return f;
        }
    }

    // ---------- CSV ----------

    public static void writeCsv(File out, String[] headers, List<String[]> rows) throws IOException {
        OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(out), StandardCharsets.UTF_8);
        w.write('\uFEFF'); // BOM ليفتح الملف بترميز صحيح في Excel
        w.write(csvLine(headers));
        for (String[] row : rows) w.write(csvLine(row));
        w.close();
    }

    private static String csvLine(String[] cells) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) b.append(',');
            String v = cells[i] == null ? "" : cells[i];
            if (v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r"))
                v = "\"" + v.replace("\"", "\"\"") + "\"";
            b.append(v);
        }
        b.append("\r\n");
        return b.toString();
    }

    public static List<String[]> readCsv(InputStream in) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) b.write(buf, 0, n);
        String s = new String(b.toByteArray(), StandardCharsets.UTF_8);
        if (s.startsWith("\uFEFF")) s = s.substring(1);
        // اكتشاف الفاصل تلقائياً من أول سطر: فاصلة أو فواصل منقوطة أو جدولة
        char delim = ',';
        String firstLine = s.split("\n", 2)[0];
        int best = 0;
        for (char cand : new char[]{',', ';', '\t'}) {
            int cnt = 0;
            for (char ch : firstLine.toCharArray()) if (ch == cand) cnt++;
            if (cnt > best) { best = cnt; delim = cand; }
        }
        List<String[]> rows = new ArrayList<>();
        List<String> cur = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (quoted) {
                if (ch == '"') {
                    if (i + 1 < s.length() && s.charAt(i + 1) == '"') { cell.append('"'); i++; }
                    else quoted = false;
                } else cell.append(ch);
            } else {
                if (ch == '"') quoted = true;
                else if (ch == delim) { cur.add(cell.toString()); cell.setLength(0); }
                else if (ch == '\r') { /* تجاهل */ }
                else if (ch == '\n') {
                    cur.add(cell.toString());
                    cell.setLength(0);
                    rows.add(cur.toArray(new String[0]));
                    cur = new ArrayList<>();
                } else cell.append(ch);
            }
        }
        if (cell.length() > 0 || !cur.isEmpty()) {
            cur.add(cell.toString());
            rows.add(cur.toArray(new String[0]));
        }
        return rows;
    }
}
