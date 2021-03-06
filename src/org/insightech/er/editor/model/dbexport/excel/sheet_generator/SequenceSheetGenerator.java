package org.insightech.er.editor.model.dbexport.excel.sheet_generator;

import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.insightech.er.db.DBManager;
import org.insightech.er.db.DBManagerFactory;
import org.insightech.er.db.impl.h2.H2DBManager;
import org.insightech.er.editor.model.ERDiagram;
import org.insightech.er.editor.model.ObjectModel;
import org.insightech.er.editor.model.dbexport.excel.ExportToExcelManager.LoopDefinition;
import org.insightech.er.editor.model.diagram_contents.not_element.sequence.Sequence;
import org.insightech.er.editor.model.progress_monitor.ProgressMonitor;
import org.insightech.er.util.Format;
import org.insightech.er.util.POIUtils;

public class SequenceSheetGenerator extends AbstractSheetGenerator {

    private static final String KEYWORD_SEQUENCE_NAME = "$PSN";

    private static final String KEYWORD_SEQUENCE_DESCRIPTION = "$SDSC";

    private static final String KEYWORD_INCREMENT = "$INC";

    private static final String KEYWORD_MIN = "$MIN";

    private static final String KEYWORD_MAX = "$MAX";

    private static final String KEYWORD_START = "$STR";

    private static final String KEYWORD_CACHE = "$CACHE";

    private static final String KEYWORD_CYCLE = "$CYC";

    /**
     * �V�[�P���X�V�[�g�Ƀf�[�^��ݒ肵�܂�.
     * 
     * @param workbook
     * @param sheet
     * @param sequence
     */
    public void setSequenceData(final HSSFWorkbook workbook, final HSSFSheet sheet, final Sequence sequence, final ERDiagram diagram) {
        String cache = Format.toString(sequence.getCache());

        if (DBManagerFactory.getDBManager(diagram).isSupported(DBManager.SUPPORT_SEQUENCE_NOCACHE)) {
            if (sequence.isNocache()) {
                cache = "NO CACHE";
            }
        }

        String min = Format.toString(sequence.getMinValue());
        String max = Format.toString(sequence.getMaxValue());
        String start = Format.toString(sequence.getStart());
        String cycle = String.valueOf(sequence.isCycle()).toUpperCase();

        if (H2DBManager.ID.equals(diagram.getDatabase())) {
            min = "-";
            max = "-";
            start = "-";
            cycle = "-";
        }

        POIUtils.replace(sheet, KEYWORD_SEQUENCE_NAME, getValue(keywordsValueMap, KEYWORD_SEQUENCE_NAME, sequence.getName()));
        POIUtils.replace(sheet, KEYWORD_SEQUENCE_DESCRIPTION, getValue(keywordsValueMap, KEYWORD_SEQUENCE_DESCRIPTION, sequence.getDescription()));
        POIUtils.replace(sheet, KEYWORD_INCREMENT, getValue(keywordsValueMap, KEYWORD_INCREMENT, Format.toString(sequence.getIncrement())));
        POIUtils.replace(sheet, KEYWORD_MIN, getValue(keywordsValueMap, KEYWORD_MIN, min));
        POIUtils.replace(sheet, KEYWORD_MAX, getValue(keywordsValueMap, KEYWORD_MAX, max));
        POIUtils.replace(sheet, KEYWORD_START, getValue(keywordsValueMap, KEYWORD_START, start));
        POIUtils.replace(sheet, KEYWORD_CACHE, getValue(keywordsValueMap, KEYWORD_CACHE, cache));
        POIUtils.replace(sheet, KEYWORD_CYCLE, getValue(keywordsValueMap, KEYWORD_CYCLE, cycle));
    }

    @Override
    public void generate(final ProgressMonitor monitor, final HSSFWorkbook workbook, final int sheetNo, final boolean useLogicalNameAsSheetName, final Map<String, Integer> sheetNameMap, final Map<String, ObjectModel> sheetObjectMap, final ERDiagram diagram, final Map<String, LoopDefinition> loopDefinitionMap) throws InterruptedException {

        for (final Sequence sequence : diagram.getDiagramContents().getSequenceSet()) {
            final String name = sequence.getName();

            final HSSFSheet newSheet = createNewSheet(workbook, sheetNo, name, sheetNameMap);

            final String sheetName = workbook.getSheetName(workbook.getSheetIndex(newSheet));
            monitor.subTaskWithCounter("[Sequence] " + sheetName);

            sheetObjectMap.put(sheetName, sequence);

            setSequenceData(workbook, newSheet, sequence, diagram);
            monitor.worked(1);
        }
    }

    @Override
    public String getTemplateSheetName() {
        return "sequence_template";
    }

    @Override
    public String[] getKeywords() {
        return new String[] {KEYWORD_SEQUENCE_NAME, KEYWORD_SEQUENCE_DESCRIPTION, KEYWORD_INCREMENT, KEYWORD_MIN, KEYWORD_MAX, KEYWORD_START, KEYWORD_CACHE, KEYWORD_CYCLE};
    }

    @Override
    public int getKeywordsColumnNo() {
        return 8;
    }

    @Override
    public int count(final ERDiagram diagram) {
        return diagram.getDiagramContents().getSequenceSet().getObjectList().size();
    }

}
