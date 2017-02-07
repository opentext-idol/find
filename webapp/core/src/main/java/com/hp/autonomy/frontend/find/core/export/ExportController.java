/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.ErrorModelAndViewInfo;
import com.hp.autonomy.frontend.find.core.web.RequestMapper;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.TableCell;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.sl.usermodel.TextShape;
import org.apache.poi.sl.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFAutoShape;
import org.apache.poi.xslf.usermodel.XSLFChart;
import org.apache.poi.xslf.usermodel.XSLFFreeformShape;
import org.apache.poi.xslf.usermodel.XSLFGroupShape;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFRelation;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xslf.usermodel.XSLFTableCell;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTDoughnutChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumData;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumRef;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumVal;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPieSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrData;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrRef;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrVal;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientStop;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientStopList;
import org.openxmlformats.schemas.drawingml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@RequestMapping(ExportController.EXPORT_PATH)
public abstract class ExportController<R extends QueryRequest<?>, E extends Exception> {
    static final String EXPORT_PATH = "/api/bi/export";
    static final String CSV_PATH = "/csv";
    static final String PPT_TOPICMAP_PATH = "/ppt/topicmap";
    static final String PPT_SUNBURST_PATH = "/ppt/sunburst";
    static final String PPT_TABLE_PATH = "/ppt/table";
    static final String PPT_MAP_PATH = "/ppt/map";
    static final String PPT_LIST_PATH = "/ppt/list";
    static final String PPT_DATEGRAPH_PATH = "/ppt/dategraph";
    static final String SELECTED_EXPORT_FIELDS_PARAM = "selectedFieldIds";
    static final String QUERY_REQUEST_PARAM = "queryRequest";
    private static final String EXPORT_FILE_NAME = "query-results";

    private final ExportService<R, E> exportService;
    private final RequestMapper<R> requestMapper;
    private final ControllerUtils controllerUtils;

    protected ExportController(final ExportService<R, E> exportService, final RequestMapper<R> requestMapper, final ControllerUtils controllerUtils) {
        this.exportService = exportService;
        this.requestMapper = requestMapper;
        this.controllerUtils = controllerUtils;
    }

    @RequestMapping(value = CSV_PATH, method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<byte[]> exportToCsv(
            @RequestParam(QUERY_REQUEST_PARAM) final String queryRequestJSON,
            // required = false to prevent Spring errors if the user asks for a CSV with no fields marked for export.
            // The UI should not allow the User to send a request for a CSV with nothing in it.
            @RequestParam(value = SELECTED_EXPORT_FIELDS_PARAM, required = false) final Collection<String> selectedFieldNames
    ) throws IOException, E {
        return export(queryRequestJSON, ExportFormat.CSV, selectedFieldNames);
    }

    private ResponseEntity<byte[]> export(final String queryRequestJSON, final ExportFormat exportFormat, final Collection<String> selectedFieldNames) throws IOException, E {
        final R queryRequest = requestMapper.parseQueryRequest(queryRequestJSON);

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exportService.export(outputStream, queryRequest, exportFormat, selectedFieldNames);
        final byte[] output = outputStream.toByteArray();

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(exportFormat.getMimeType()));
        final String fileName = EXPORT_FILE_NAME + FilenameUtils.EXTENSION_SEPARATOR + exportFormat.getExtension();
        headers.setContentDispositionFormData(fileName, fileName);

        return new ResponseEntity<>(output, headers, HttpStatus.OK);
    }

    //TODO improve to inform what went wrong with export, rather than generic just error 500.
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleException(
            final Exception e,
            final HttpServletRequest request,
            final ServletResponse response
    ) {
        response.reset();

        return controllerUtils.buildErrorModelAndView(new ErrorModelAndViewInfo.Builder()
                .setRequest(request)
                .setMainMessageCode("error.internalServerErrorMain")
                .setSubMessageCode("error.internalServerErrorSub")
                .setSubMessageArguments(null)
                .setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setContactSupport(true)
                .setException(e)
                .build());
    }

    @RequestMapping(value = PPT_TOPICMAP_PATH, method = RequestMethod.POST)
    public HttpEntity<byte[]> topicmap(
            @RequestParam("paths") final String pathStr
    ) throws IOException {
        final Path[] paths = new ObjectMapper().readValue(pathStr, Path[].class);

        final XMLSlideShow ppt = loadTemplate(false, false);
        final Dimension pageSize = ppt.getPageSize();
        final XSLFSlide slide = ppt.createSlide();

        drawTopicMap(slide, new Rectangle2D.Double(0, 0, pageSize.getWidth(), pageSize.getHeight()), paths);

        return writePPT(ppt, "topicmap.pptx");
    }

    private void drawTopicMap(final XSLFSlide slide, final Rectangle2D.Double anchor, final Path[] paths) {
        for(final Path reqPath : paths) {
            final XSLFFreeformShape shape = slide.createFreeform();
            final Path2D.Double path = new Path2D.Double();

            boolean first = true;

            for(double[] point : reqPath.getPoints()) {
                final double x = point[0] * anchor.getWidth() + anchor.getMinX();
                final double y = point[1] * anchor.getHeight() + anchor.getMinY();
                if(first) {
                    path.moveTo(x, y);
                    first = false;
                }
                else {
                    path.lineTo(x, y);
                }
            }
            path.closePath();

            shape.setPath(path);
            shape.setStrokeStyle(2);
            shape.setLineColor(Color.GRAY);
            shape.setHorizontalCentered(true);
            shape.setVerticalAlignment(VerticalAlignment.MIDDLE);
            shape.setTextAutofit(TextShape.TextAutofit.NORMAL);

            final XSLFTextParagraph text = shape.addNewTextParagraph();
            final XSLFTextRun textRun = text.addNewTextRun();
            textRun.setText(reqPath.name);
            textRun.setFontColor(Color.WHITE);
            textRun.setBold(true);

            final int opacity = (int) (100000 * reqPath.getOpacity());
            final Color c1 = Color.decode(reqPath.getColor());
            final Color c2 = Color.decode(reqPath.getColor2());

            final CTShape cs = (CTShape) shape.getXmlObject();
            final CTGradientFillProperties gFill = cs.getSpPr().addNewGradFill();
            gFill.addNewLin().setAng(3300000);
            final CTGradientStopList list = gFill.addNewGsLst();

            final CTGradientStop stop1 = list.addNewGs();
            stop1.setPos(0);
            final CTSRgbColor color1 = stop1.addNewSrgbClr();
            color1.setVal(new byte[]{(byte) c1.getRed(), (byte) c1.getGreen(), (byte) c1.getBlue()});
            color1.addNewAlpha().setVal(opacity);

            final CTGradientStop stop2 = list.addNewGs();
            stop2.setPos(100000);
            final CTSRgbColor color2 = stop2.addNewSrgbClr();
            color2.setVal(new byte[]{(byte) c2.getRed(), (byte) c2.getGreen(), (byte) c2.getBlue()});
            color2.addNewAlpha().setVal(opacity);
        }
    }

    private HttpEntity<byte[]> writePPT(final XMLSlideShow ppt, final String filename) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ppt.write(baos);
        ppt.close();

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.presentationml.presentation"));
        headers.set("Content-Disposition", "inline; filename=" + filename);
        return new HttpEntity<>(baos.toByteArray(), headers);
    }

    @RequestMapping(value = PPT_SUNBURST_PATH, method = RequestMethod.POST)
    public HttpEntity<byte[]> sunburst(
            @RequestParam("title") final String title,
            @RequestParam("data") final String dataStr
    ) throws IOException {
        final SunburstData data = new ObjectMapper().readValue(dataStr, SunburstData.class);

        if(!data.validateInput()) {
            throw new IllegalArgumentException("Number of values should match the number of categories");
        }

        final String[] categories = data.getCategories();
        final double[] values = data.getValues();

        final XMLSlideShow ppt = loadTemplate(true, false);

        final XSLFSlide slide = ppt.getSlides().get(0);

        XSLFChart chart = null;
        for(POIXMLDocumentPart part : slide.getRelations()) {
            if(part instanceof XSLFChart) {
                chart = (XSLFChart) part;
                break;
            }
        }

        if(chart == null) throw new IllegalStateException("Chart required in template");

        final XSSFWorkbook workbook = new XSSFWorkbook();
        final XSSFSheet sheet = workbook.createSheet();

        final CTChart ctChart = chart.getCTChart();
        final CTPlotArea plotArea = ctChart.getPlotArea();

        final CTDoughnutChart donutChart = plotArea.getDoughnutChartArray(0);

        final CTPieSer series = donutChart.getSerArray(0);

        final CTStrRef strRef = series.getTx().getStrRef();
        strRef.getStrCache().getPtArray(0).setV(title);
        sheet.createRow(0).createCell(1).setCellValue(title);
        strRef.setF(new CellReference(sheet.getSheetName(), 0, 1, true, true).formatAsString());

        final CTStrRef categoryRef = series.getCat().getStrRef();
        final CTStrData categoryData = categoryRef.getStrCache();
        final CTNumRef numRef = series.getVal().getNumRef();
        final CTNumData numericData = numRef.getNumCache();

        categoryData.setPtArray(null);
        numericData.setPtArray(null);

        for(int idx = 0; idx < values.length; ++idx) {
            final CTStrVal categoryPoint = categoryData.addNewPt();
            categoryPoint.setIdx(idx);
            categoryPoint.setV(categories[idx]);

            final CTNumVal numericPoint = numericData.addNewPt();
            numericPoint.setIdx(idx);
            numericPoint.setV(Double.toString(values[idx]));

            XSSFRow row = sheet.createRow(idx + 1);
            row.createCell(0).setCellValue(categories[idx]);
            row.createCell(1).setCellValue(values[idx]);
        }
        categoryData.getPtCount().setVal(categories.length);
        numericData.getPtCount().setVal(values.length);

        categoryRef.setF(new CellRangeAddress(1, values.length, 0, 0).formatAsString(sheet.getSheetName(), true));
        numRef.setF(new CellRangeAddress(1, values.length, 1, 1).formatAsString(sheet.getSheetName(), true));

        for(final POIXMLDocumentPart part : chart.getRelations()) {
            final PackagePart pkg = part.getPackagePart();
            if("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(pkg.getContentType())) {
                // We have to rewrite the chart data; OpenOffice doesn't use it but Powerpoint does when you click 'Edit Data'
                try(final OutputStream xlsOut = pkg.getOutputStream()) {
                    workbook.write(xlsOut);
                }
                break;
            }
        }

        return writePPT(ppt, "sunburst.pptx");
    }

    @RequestMapping(value = PPT_TABLE_PATH, method = RequestMethod.POST)
    public HttpEntity<byte[]> table(
            @RequestParam("title") final String title,
            @RequestParam("rows") final int rows,
            @RequestParam("cols") final int cols,
            @RequestParam("d") final String[] data
    ) throws IOException {
        if(data.length != rows * cols) {
            throw new IllegalArgumentException("Number of data points does not match the number of columns");
        }

        final XMLSlideShow ppt = loadTemplate(false, false);
        final Dimension pageSize = ppt.getPageSize();
        final double pageWidth = pageSize.getWidth(), pageHeight = pageSize.getHeight();
        final XSLFSlide sl = ppt.createSlide();

        final XSLFTextBox textBox = sl.createTextBox();
        textBox.setText(title);
        textBox.setHorizontalCentered(true);
        textBox.setTextAutofit(TextShape.TextAutofit.SHAPE);
        final Rectangle2D.Double textBounds = new Rectangle2D.Double(0, 0.05 * pageHeight, pageWidth, 0.1 * pageHeight);
        textBox.setAnchor(textBounds);

        final XSLFTable table = sl.createTable(rows, cols);

        int idx = 0;

        for(int row = 0; row < rows; ++row) {
            for(int col = 0; col < cols; ++col) {
                final XSLFTableCell cell = table.getCell(row, col);
                cell.setText(data[idx++]);

                for(final TableCell.BorderEdge edge : TableCell.BorderEdge.values()) {
                    cell.setBorderColor(edge, Color.BLACK);
                }
            }
        }

        double tableW = 0, tableH = 0;

        for(int col = 0; col < cols; ++col) {
            table.setColumnWidth(col, pageWidth / cols * 0.8);
            tableW += table.getColumnWidth(col);
        }

        for(int row = 0; row < rows; ++row) {
            tableH += table.getRowHeight(row);
        }

        table.setAnchor(new Rectangle2D.Double(0.5 * (pageWidth - tableW), textBounds.getMaxY(), tableW, Math.min(tableH, pageHeight - textBounds.getMaxY())));

        return writePPT(ppt, "table.pptx");
    }

    @RequestMapping(value = PPT_MAP_PATH, method = RequestMethod.POST)
    public HttpEntity<byte[]> map(
            @RequestParam("title") final String title,
            @RequestParam("image") final String image,
            @RequestParam(value = "markers", defaultValue = "[]") final String markerStr
    ) throws IOException {
        final Marker[] markers = new ObjectMapper().readValue(markerStr, Marker[].class);

        final XMLSlideShow ppt = loadTemplate(false, false);
        final Dimension pageSize = ppt.getPageSize();
        final double pageWidth = pageSize.getWidth(), pageHeight = pageSize.getHeight();
        final XSLFSlide sl = ppt.createSlide();

        final XSLFTextBox textBox = sl.createTextBox();
        textBox.clearText();
        final XSLFTextParagraph paragraph = textBox.addNewTextParagraph();
        paragraph.setTextAlign(TextParagraph.TextAlign.CENTER);
        paragraph.addNewTextRun().setText(title);
        textBox.setHorizontalCentered(true);
        textBox.setTextAutofit(TextShape.TextAutofit.SHAPE);
        final Rectangle2D.Double textBounds = new Rectangle2D.Double(0, 0.05 * pageHeight, pageWidth, 0.1 * pageHeight);
        textBox.setAnchor(textBounds);

        final PictureData.PictureType type;
        if(image.startsWith("data:image/png;base64,")) {
            type = PictureData.PictureType.PNG;
        }
        else if(image.startsWith("data:image/jpeg;base64,")) {
            type = PictureData.PictureType.JPEG;
        }
        else {
            throw new IllegalArgumentException("Unsupported image type");
        }

        final byte[] bytes = Base64.decodeBase64(image.split(",")[1]);
        final XSLFPictureData picture = ppt.addPicture(bytes, type);
        final double offsetY = textBounds.getMaxY();
        addMap(sl, new Rectangle2D.Double(0, offsetY, pageWidth, pageHeight - textBounds.getMaxY()), picture, markers);

        return writePPT(ppt, "map.pptx");
    }

    private XSLFPictureShape addMap(final XSLFSlide slide, final Rectangle2D.Double anchor, final XSLFPictureData picture, final Marker[] markers) {
        double tgtW = anchor.getWidth(),
               tgtH = anchor.getHeight(),
               offsetX = anchor.getMinX(),
               offsetY = anchor.getMinY();

        final Dimension size = picture.getImageDimension();
        final double ratio = size.getWidth() / size.getHeight();

        if(ratio > tgtW / tgtH) {
            // source image is wider than target, clip fixed width variable height
            tgtH = tgtW / ratio;
        }
        else {
            tgtW = tgtH * ratio;
        }

        final XSLFPictureShape canvas = slide.createPicture(picture);
        // Vertically align top, horizontally-align center
        canvas.setAnchor(new Rectangle2D.Double(offsetX + 0.5 * (anchor.getWidth() - tgtW), offsetY, tgtW, tgtH));

        for(Marker marker : markers) {
            final Color color = Color.decode(marker.color);
            final double centerX = offsetX + marker.x * tgtW;
            final double centerY = offsetY + marker.y * tgtH;

            if(marker.isCluster()) {
                final XSLFGroupShape group = slide.createGroup();
                double halfMark = 10;
                double mark = halfMark * 2;
                double innerHalfMark = 7;
                double innerMark = innerHalfMark * 2;
                // align these so the middle is the latlng position
                final Rectangle2D.Double groupAnchor = new Rectangle2D.Double(centerX - halfMark, centerY - halfMark, mark, mark);

                group.setAnchor(groupAnchor);
                group.setInteriorAnchor(groupAnchor);

                final XSLFAutoShape shape = group.createAutoShape();
                shape.setShapeType(ShapeType.ELLIPSE);
                final boolean fade = marker.isFade();
                // There's a 0.3 alpha transparency (255 * 0.3 is 76) when a marker is faded out
                final int FADE_ALPHA = 76;
                shape.setFillColor(transparentColor(color, fade ? 47 : 154));
                shape.setAnchor(groupAnchor);

                final XSLFAutoShape inner = group.createAutoShape();
                inner.setFillColor(fade ? transparentColor(color, FADE_ALPHA) : color);
                inner.setLineWidth(0.1);
                inner.setLineColor(new Color((int) (color.getRed() * 0.9), (int) (color.getGreen() * 0.9), (int) (color.getBlue() * 0.9), fade ? FADE_ALPHA : 255));
                inner.setShapeType(ShapeType.ELLIPSE);
                inner.setHorizontalCentered(true);
                inner.setWordWrap(false);
                inner.setVerticalAlignment(VerticalAlignment.MIDDLE);
                inner.clearText();
                final XSLFTextParagraph para = inner.addNewTextParagraph();
                para.setTextAlign(TextParagraph.TextAlign.CENTER);
                final XSLFTextRun text = para.addNewTextRun();
                text.setFontSize(6.0);
                final Color fontColor = Color.decode(StringUtils.defaultString(marker.getFontColor(), "#000000"));
                text.setFontColor(fade ? transparentColor(fontColor, FADE_ALPHA) : fontColor);
                text.setText(marker.getText());
                inner.setAnchor(new Rectangle2D.Double(centerX - innerHalfMark, centerY - innerHalfMark, innerMark, innerMark));
            }
            else {
                final XSLFAutoShape shape = slide.createAutoShape();
                shape.setHorizontalCentered(true);
                shape.setWordWrap(false);
                shape.setShapeType(ShapeType.TEARDROP);
                shape.setVerticalAlignment(VerticalAlignment.BOTTOM);
                shape.setRotation(135);
                shape.setLineWidth(1.0);
                shape.setLineColor(color.darker());
                shape.setFillColor(transparentColor(color, 210));
                double halfMark = 8;
                double mark = halfMark * 2;
                // align these so the pointy end at the bottom is the latlng position
                shape.setAnchor(new Rectangle2D.Double(centerX - halfMark, centerY - mark, mark, mark));

                // We create a hyperlink which links back to this slide; so we get hover-over-detail-text on the marker
                final CTHyperlink link = ((CTShape) shape.getXmlObject()).getNvSpPr().getCNvPr().addNewHlinkClick();
                link.setTooltip(marker.getText());
                final PackageRelationship rel = shape.getSheet().getPackagePart().addRelationship(slide.getPackagePart().getPartName(),
                        TargetMode.INTERNAL, XSLFRelation.SLIDE.getRelation());
                link.setId(rel.getId());
                link.setAction("ppaction://hlinksldjump");
            }
        }

        return canvas;
    }

    private static Color transparentColor(final Color color, final int a) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), a);
    }

    @RequestMapping(value = PPT_LIST_PATH, method = RequestMethod.POST)
    public HttpEntity<byte[]> list(
            @RequestParam("results") final String results,
            @RequestParam("sortBy") final String sortBy,
            @RequestParam(value = "docs", defaultValue = "[]") final String docsStr
    ) throws IOException {
        final Document[] docs = new ObjectMapper().readValue(docsStr, Document[].class);

        final XMLSlideShow ppt = loadTemplate(false, false);
        final Dimension pageSize = ppt.getPageSize();
        final double
                pageWidth = pageSize.getWidth(), pageHeight = pageSize.getHeight(),
                // How much space to leave at the left and right edge of the slide
                xMargin = 20,
                // Size of the icon
                iconWidth = 20, iconHeight = 24,
                // Find's thumbnail height is 97px by 55px, hardcoded in the CSS in .document-thumbnail
                thumbScale = 0.8,
                thumbW = 97 * thumbScale, thumbH = 55 * thumbScale,
                // Margin around the thumbnail
                thumbMargin = 4.,
                // Space between list items
                listItemMargin = 5.;

        final Pattern highlightPattern = Pattern.compile("<HavenSearch-QueryText-Placeholder>(.*?)</HavenSearch-QueryText-Placeholder>");

        XSLFSlide sl = null;
        double yCursor = 0, xCursor = 0;

        int docsOnPage = 0;

        for(int docIdx = 0; docIdx < docs.length; ++docIdx) {
            final Document doc = docs[docIdx];

            if(sl == null) {
                sl = ppt.createSlide();
                yCursor = 5;
                xCursor = xMargin;
                docsOnPage = 0;

                final XSLFTextBox textBox = sl.createTextBox();
                textBox.clearText();
                final Rectangle2D.Double textBounds = new Rectangle2D.Double(xMargin, yCursor, pageWidth - xMargin, 20);
                textBox.setAnchor(textBounds);

                addTextRun(textBox.addNewTextParagraph(), results, 12., Color.LIGHT_GRAY);

                final XSLFTextBox sortByEl = sl.createTextBox();
                sortByEl.clearText();
                final XSLFTextParagraph sortByText = sortByEl.addNewTextParagraph();
                sortByText.setTextAlign(TextParagraph.TextAlign.RIGHT);

                addTextRun(sortByText, sortBy, 12., Color.LIGHT_GRAY);

                sortByEl.setAnchor(new Rectangle2D.Double(xMargin, yCursor, pageWidth - xMargin, 20));

                yCursor += listItemMargin + Math.max(
                        textBox.getTextHeight() + textBox.getAnchor().getY(),
                        sortByEl.getTextHeight() + sortByEl.getAnchor().getY());
            }

            final XSLFAutoShape icon = sl.createAutoShape();
            icon.setShapeType(ShapeType.SNIP_1_RECT);
            icon.setAnchor(new Rectangle2D.Double(xCursor, yCursor + listItemMargin, iconWidth, iconHeight));
            icon.setLineColor(Color.decode("#888888"));
            icon.setLineWidth(2.0);

            xCursor += iconWidth;

            final XSLFTextBox listEl = sl.createTextBox();
            listEl.clearText();
            listEl.setAnchor(new Rectangle2D.Double(xCursor, yCursor, pageWidth - xCursor - xMargin, pageHeight - yCursor));

            final XSLFTextParagraph titlePara = listEl.addNewTextParagraph();
            addTextRun(titlePara, doc.getTitle(), 14.0, Color.BLACK).setBold(true);

            if (StringUtils.isNotBlank(doc.getDate())) {
                final XSLFTextParagraph datePara = listEl.addNewTextParagraph();
                datePara.setLeftMargin(5.);
                addTextRun(datePara, doc.getDate(), 10., Color.GRAY).setItalic(true);
            }

            if (StringUtils.isNotBlank(doc.getRef())) {
                addTextRun(listEl.addNewTextParagraph(), doc.getRef(), 12., Color.GRAY);
            }

            final double thumbnailOffset = listEl.getTextHeight();

            final XSLFTextParagraph contentPara = listEl.addNewTextParagraph();

            XSLFPictureShape picture = null;

            if (StringUtils.isNotBlank(doc.getThumbnail())) {
                try {
                    final byte[] imageData = Base64.decodeBase64(doc.getThumbnail());
                    // Picture reuse is automatic
                    picture = sl.createPicture(ppt.addPicture(imageData, PictureData.PictureType.JPEG));
                    picture.setAnchor(new Rectangle2D.Double(xCursor, yCursor + thumbnailOffset + thumbMargin, thumbW, thumbH));
                    contentPara.setLeftMargin(thumbW);
                }
                catch(RuntimeException e) {
                    // if there's any errors, we'll just ignore the image
                }
            }

            final String rawSummary = doc.getSummary();
            if (StringUtils.isNotBlank(rawSummary)) {
                // HTML treats newlines and multiple whitespace as a single whitespace.
                final String summary = rawSummary.replaceAll("\\s+", " ");
                final Matcher matcher = highlightPattern.matcher(summary);
                int idx = 0;

                while(matcher.find()) {
                    final int start = matcher.start();

                    if (idx < start) {
                        addTextRun(contentPara, summary.substring(idx, start), 12., Color.DARK_GRAY);
                    }

                    addTextRun(contentPara, matcher.group(1), 12., Color.DARK_GRAY).setBold(true);
                    idx = matcher.end();
                }

                if (idx < summary.length()) {
                    addTextRun(contentPara, summary.substring(idx), 12., Color.DARK_GRAY);
                }
            }

            double elHeight = Math.max(listEl.getTextHeight(), iconHeight);
            if (picture != null) {
                elHeight = Math.max(elHeight, picture.getAnchor().getMaxY() - yCursor);
            }

            yCursor += elHeight;
            xCursor = xMargin;

            docsOnPage++;

            if (yCursor > pageHeight) {
                if (docsOnPage > 1) {
                    // If we drew more than one list element on this page; and we exceeded the available space,
                    //   delete the last element's shapes and redraw it on the next page.
                    sl.removeShape(listEl);
                    sl.removeShape(icon);

                    if (picture != null) {
                        // Technically we want to remove the shape, but that also removes the related image data,
                        //   which will be shared with other images; causing problems when trying to render them.
                        // Workaround is to just hide the image out of view.
                        picture.setAnchor(new Rectangle2D.Double(pageWidth, pageHeight, 0.1, 0.1));
                    }
                    --docIdx;
                }

                sl = null;
            }
            else {
                yCursor += listItemMargin;
            }
        }

        return writePPT(ppt, "list.pptx");
    }

    private static XSLFTextRun addTextRun(final XSLFTextParagraph paragraph, final String text, final double fontSize, final Color color) {
        final XSLFTextRun summary = paragraph.addNewTextRun();
        summary.setFontColor(color);
        summary.setText(text);
        summary.setFontSize(fontSize);
        return summary;
    }

    @Value(value = "classpath:/templates/template.pptx")
    private Resource pptxTemplate;

    private XMLSlideShow loadTemplate(final boolean keepPieChart, final boolean keepGraph) throws IOException {
        try(InputStream inputStream = pptxTemplate.getInputStream()) {
            final XMLSlideShow ppt = new XMLSlideShow(inputStream);

            // The template should have two slides. The first slide should have a pie chart; the second has a graph.
            if (ppt.getSlides().size() != 2) {
                throw new IllegalArgumentException("Invalid template supplied");
            }

            if (!keepGraph) {
                ppt.removeSlide(1);
            }

            if (!keepPieChart) {
                ppt.removeSlide(0);
            }

            return ppt;
        }
    }

    @RequestMapping(value = PPT_DATEGRAPH_PATH, method = RequestMethod.POST)
    public HttpEntity<byte[]> graph(
            @RequestParam("data") final String dataStr
    ) throws IOException {
        final ChartData data = new ObjectMapper().readValue(dataStr, ChartData.class);

        if (!data.validateInput()) {
            throw new IllegalArgumentException("Invalid data provided");
        }

        boolean useSecondaryAxis = data.rows.stream().anyMatch(ChartData.Row::isSecondaryAxis);

        if (data.rows.stream().allMatch(ChartData.Row::isSecondaryAxis)) {
            // If everything is on the secondary axis; just use the primary axis
            data.rows.forEach(row -> row.setSecondaryAxis(false));
            useSecondaryAxis = false;
        }

        final XSSFWorkbook wb = writeChart(data);

        final XMLSlideShow ppt = loadTemplate(false, true);

        final XSLFSlide slide = ppt.getSlides().get(0);

        XSLFChart chart = null;

        for(POIXMLDocumentPart part : slide.getRelations()) {
            if (part instanceof XSLFChart) {
                chart = (XSLFChart) part;
                break;
            }
        }

        if (chart == null) {
            throw new IllegalArgumentException("Invalid template supplied");
        }

        final CTChart ctChart = chart.getCTChart();
        final CTPlotArea plotArea = ctChart.getPlotArea();
        final XSSFSheet sheet = wb.getSheetAt(0);

        // In the template, we have two <c:lineChart> objects, one for the primary axis, one for the secondary.
        if (!useSecondaryAxis) {
            // Discard the extra axes
            // OpenOffice is happy enough if you remove the line chart, but PowerPoint will complain it's a corrupt
            //   file and unhelpfully delete the entire chart when you choose 'repair' if any orphan axes remain.
            plotArea.removeLineChart(1);
            plotArea.removeValAx(1);
            plotArea.removeDateAx(1);
        }

        final CTLineChart primaryChart = plotArea.getLineChartArray()[0];
        final CTLineSer[] primarySeries = primaryChart.getSerArray();
        primarySeries[0].getDPtList().clear();

        int primarySeriesCount = 0;
        int secondarySeriesCount = 0;

        for (int seriesIdx = 0; seriesIdx < data.rows.size(); ++seriesIdx) {
            final ChartData.Row row = data.rows.get(seriesIdx);

            final CTLineChart tgtChart = plotArea.getLineChartArray(row.isSecondaryAxis() ? 1 : 0);

            final CTLineSer[] serArray = tgtChart.getSerArray();
            final int createdSeriesIdx = row.isSecondaryAxis() ? secondarySeriesCount++ : primarySeriesCount++;

            final CTLineSer curSeries;

            if (createdSeriesIdx < serArray.length) {
                curSeries = serArray[createdSeriesIdx];
            }
            else {
                curSeries = tgtChart.addNewSer();
                curSeries.set(serArray[0].copy());
            }

            updateCTLineSer(data, sheet, seriesIdx, curSeries);
        }

        for(final POIXMLDocumentPart rel : chart.getRelations()) {
            final PackagePart pkg = rel.getPackagePart();
            if ("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(pkg.getContentType())) {
                wb.write(pkg.getOutputStream());
            }
        }

        return writePPT(ppt, "dategraph.pptx");
    }

    private static void updateCTLineSer(final ChartData data, final XSSFSheet sheet, final int seriesIdx, final CTLineSer series) {
        final String sheetName = sheet.getSheetName();

        // the series idx starts from 0
        final ChartData.Row row = data.getRows().get(seriesIdx);
        final String title = row.getLabel();
        final Color color = Color.decode(row.getColor());

        series.getOrder().setVal(seriesIdx);
        series.getIdx().setVal(seriesIdx);

        final CTSolidColorFillProperties fill = series.getSpPr().getLn().getSolidFill();

        // We have to set any possible colour type, PowerPoint throws an error if there's multiple fills, and we don't
        //   know what colour type the user may have used in their template slide.
        if (fill.getSchemeClr() != null) {
            fill.unsetSchemeClr();
        }
        if (fill.getSrgbClr() != null) {
            fill.unsetSrgbClr();
        }
        if (fill.getHslClr() != null) {
            fill.unsetHslClr();
        }
        if (fill.getPrstClr() != null) {
            fill.unsetPrstClr();
        }
        if (fill.getScrgbClr() != null) {
            fill.unsetScrgbClr();
        }
        if (fill.getPrstClr() != null) {
            fill.unsetPrstClr();
        }

        final CTSRgbColor fillClr = fill.addNewSrgbClr();
        fillClr.setVal(new byte[]{ (byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue()});

        final CTStrRef strRef = series.getTx().getStrRef();
        strRef.getStrCache().getPtArray()[0].setV(title);

        strRef.setF(new CellReference(sheetName, 0, seriesIdx + 1, true, true).formatAsString());

        {
            final CTNumRef timestampCatNumRef = series.getCat().getNumRef();
            timestampCatNumRef.setF(new AreaReference(
                new CellReference(sheetName, 1, 0, true, true),
                new CellReference(sheetName, 1 + data.timestamps.length, 0, true, true)
            ).formatAsString());

            final CTNumData timeStampCatNumCache = timestampCatNumRef.getNumCache();
            timeStampCatNumCache.getPtCount().setVal(data.timestamps.length);
            timeStampCatNumCache.setPtArray(null);

            for(int ii = 0; ii < data.timestamps.length; ++ii) {
                final CTNumVal pt = timeStampCatNumCache.addNewPt();
                pt.setIdx(ii);
                pt.setV(sheet.getRow(1 + ii).getCell(0).getRawValue());
            }
        }

        {
            final double[] seriesData = row.getValues();

            final CTNumRef valuesNumRef = series.getVal().getNumRef();
            valuesNumRef.setF(new AreaReference(
                new CellReference(sheetName, 1, seriesIdx + 1, true, true),
                new CellReference(sheetName, 1 + data.timestamps.length, seriesIdx + 1, true, true)
            ).formatAsString());

            final CTNumData valuesNumCache = valuesNumRef.getNumCache();
            valuesNumCache.getPtCount().setVal(data.timestamps.length);
            valuesNumCache.setPtArray(null);

            for(int ii = 0; ii < data.timestamps.length; ++ii) {
                final CTNumVal pt = valuesNumCache.addNewPt();
                pt.setIdx(ii);
                pt.setV(Double.toString(seriesData[ii]));
            }
        }
    }

    private static XSSFWorkbook writeChart(final ChartData data) {
        final XSSFWorkbook wb = new XSSFWorkbook();
        final XSSFSheet sheet = wb.createSheet("Sheet1");

        final CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setDataFormat((short) 14);

        final XSSFRow header = sheet.createRow(0);
        header.createCell(0).setCellValue("Timestamp");
        for (int ii = 0; ii < data.rows.size(); ++ii) {
            header.createCell(ii + 1).setCellValue(data.rows.get(ii).getLabel());
        }

        for (int rowIdx = 0; rowIdx < data.timestamps.length; ++rowIdx) {
            final XSSFRow row = sheet.createRow(rowIdx + 1);

            final XSSFCell cell = row.createCell(0);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(new Date(data.timestamps[rowIdx] * 1000));

            for (int ii = 0; ii < data.rows.size(); ++ii) {
                row.createCell(ii + 1).setCellValue(data.rows.get(ii).getValues()[rowIdx]);
            }
        }

        return wb;
    }

}