package net.virtualboss.service.migration;

import com.linuxense.javadbf.DBFReader;
import com.linuxense.javadbf.DBFRow;
import com.linuxense.javadbf.DBFUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.model.entity.Field;
import net.virtualboss.model.entity.FieldValue;
import net.virtualboss.model.enums.EntityType;
import net.virtualboss.repository.FieldRepository;
import net.virtualboss.repository.FieldValueRepository;
import net.virtualboss.service.FieldService;
import net.virtualboss.web.dto.FieldDto;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class MigrationService {
    private static final String NOTES = "notes";
    private static final String NOTES_RTF = "notes_rtf";
    private static final String IS_DELETED = "is_deleted";
    private static final String COMPANY = "company";
    private static final String NULL = "\u0000";
    private static final String MODIFIED_TIME = "modified_time";
    private static final String CREATED_TIME = "created_time";

    private static final String TASK_CUSTOM_FIELD_1 = "TaskCustomField1";
    private static final String TASK_CUSTOM_FIELD_2 = "TaskCustomField2";
    private static final String TASK_CUSTOM_FIELD_3 = "TaskCustomField3";
    private static final String TASK_CUSTOM_FIELD_4 = "TaskCustomField4";
    private static final String TASK_CUSTOM_FIELD_5 = "TaskCustomField5";
    private static final String TASK_CUSTOM_FIELD_6 = "TaskCustomField6";
    private static final String TASK_CUSTOM_LIST_1 = "TaskCustomList1";
    private static final String TASK_CUSTOM_LIST_2 = "TaskCustomList2";
    private static final String TASK_CUSTOM_LIST_3 = "TaskCustomList3";
    private static final String TASK_CUSTOM_LIST_4 = "TaskCustomList4";
    private static final String TASK_CUSTOM_LIST_5 = "TaskCustomList5";
    private static final String TASK_CUSTOM_LIST_6 = "TaskCustomList6";

    private static final String JOB_CUSTOM_FIELD_1 = "JobCustomField1";
    private static final String JOB_CUSTOM_FIELD_2 = "JobCustomField2";
    private static final String JOB_CUSTOM_FIELD_3 = "JobCustomField3";
    private static final String JOB_CUSTOM_FIELD_4 = "JobCustomField4";
    private static final String JOB_CUSTOM_FIELD_5 = "JobCustomField5";
    private static final String JOB_CUSTOM_FIELD_6 = "JobCustomField6";
    private static final String JOB_CUSTOM_LIST_1 = "JobCustomList1";
    private static final String JOB_CUSTOM_LIST_2 = "JobCustomList2";
    private static final String JOB_CUSTOM_LIST_3 = "JobCustomList3";
    private static final String JOB_CUSTOM_LIST_4 = "JobCustomList4";
    private static final String JOB_CUSTOM_LIST_5 = "JobCustomList5";
    private static final String JOB_CUSTOM_LIST_6 = "JobCustomList6";

    private static final String CONTACT_CUSTOM_FIELD_1 = "ContactCustomField1";
    private static final String CONTACT_CUSTOM_FIELD_2 = "ContactCustomField2";
    private static final String CONTACT_CUSTOM_FIELD_3 = "ContactCustomField3";
    private static final String CONTACT_CUSTOM_FIELD_4 = "ContactCustomField4";
    private static final String CONTACT_CUSTOM_FIELD_5 = "ContactCustomField5";
    private static final String CONTACT_CUSTOM_FIELD_6 = "ContactCustomField6";
    private static final String CONTACT_CUSTOM_LIST_1 = "ContactCustomList1";
    private static final String CONTACT_CUSTOM_LIST_2 = "ContactCustomList2";
    private static final String CONTACT_CUSTOM_LIST_3 = "ContactCustomList3";
    private static final String CONTACT_CUSTOM_LIST_4 = "ContactCustomList4";
    private static final String CONTACT_CUSTOM_LIST_5 = "ContactCustomList5";
    private static final String CONTACT_CUSTOM_LIST_6 = "ContactCustomList6";

    private final FieldService fieldService;
    private final DBFReaderFactory dbfReaderFactory;
    private final DBConnection dbConnection;
    private final FieldValueRepository fieldValueRepository;
    private final FieldRepository fieldRepository;

    private final Map<String, String> jobCodes = new HashMap<>();
    private final Map<String, String> contactCodes = new HashMap<>();
    private final Map<String, String> employeeCodes = new HashMap<>();
    private final Map<Integer, String> taskCodes = new HashMap<>();
    private final Map<String, Integer> pendingTasks = new HashMap<>();

    private final Map<String, Field> fieldMap = new HashMap<>();
    private final Map<String, Long> entityCustomValues = new HashMap<>();


    public void migrate(String dataPath) {

        if (dataPath == null) {
            dataPath = "db/sampleJobs/SampleCompanyInc";
        }

        createFieldMap();

        migrateFields(dataPath);
        migrateJobs(dataPath);
        migrateContacts(dataPath);
        migrateEmployees(dataPath);
        migrateTasks(dataPath);
        migrateCustomValues();
    }

    private void migrateCustomValues() {
        if (entityCustomValues.isEmpty()) return;
        for (Map.Entry<String, Long> entry : entityCustomValues.entrySet()) {
            dbConnection.addRow(List.of(entry.getKey(), entry.getValue()));
            if (dbConnection.getMultiInsertLength() > 10_000_000) {
                flushDataToDatabase("entity_custom_values", List.of("entity_id", "custom_value_id"));
            }
        }
        flushDataToDatabase("entity_custom_values", List.of("entity_id", "custom_value_id"));
    }

    private void createFieldMap() {
        fieldMap.put(TASK_CUSTOM_FIELD_1, fieldRepository.findByName(TASK_CUSTOM_FIELD_1).orElseThrow());
        fieldMap.put(TASK_CUSTOM_FIELD_2, fieldRepository.findByName(TASK_CUSTOM_FIELD_2).orElseThrow());
        fieldMap.put(TASK_CUSTOM_FIELD_3, fieldRepository.findByName(TASK_CUSTOM_FIELD_3).orElseThrow());
        fieldMap.put(TASK_CUSTOM_FIELD_4, fieldRepository.findByName(TASK_CUSTOM_FIELD_4).orElseThrow());
        fieldMap.put(TASK_CUSTOM_FIELD_5, fieldRepository.findByName(TASK_CUSTOM_FIELD_5).orElseThrow());
        fieldMap.put(TASK_CUSTOM_FIELD_6, fieldRepository.findByName(TASK_CUSTOM_FIELD_6).orElseThrow());
        fieldMap.put(TASK_CUSTOM_LIST_1, fieldRepository.findByName(TASK_CUSTOM_LIST_1).orElseThrow());
        fieldMap.put(TASK_CUSTOM_LIST_2, fieldRepository.findByName(TASK_CUSTOM_LIST_2).orElseThrow());
        fieldMap.put(TASK_CUSTOM_LIST_3, fieldRepository.findByName(TASK_CUSTOM_LIST_3).orElseThrow());
        fieldMap.put(TASK_CUSTOM_LIST_4, fieldRepository.findByName(TASK_CUSTOM_LIST_4).orElseThrow());
        fieldMap.put(TASK_CUSTOM_LIST_5, fieldRepository.findByName(TASK_CUSTOM_LIST_5).orElseThrow());
        fieldMap.put(TASK_CUSTOM_LIST_6, fieldRepository.findByName(TASK_CUSTOM_LIST_6).orElseThrow());

        fieldMap.put(CONTACT_CUSTOM_FIELD_1, fieldRepository.findByName(CONTACT_CUSTOM_FIELD_1).orElseThrow());
        fieldMap.put(CONTACT_CUSTOM_FIELD_2, fieldRepository.findByName(CONTACT_CUSTOM_FIELD_2).orElseThrow());
        fieldMap.put(CONTACT_CUSTOM_FIELD_3, fieldRepository.findByName(CONTACT_CUSTOM_FIELD_3).orElseThrow());
        fieldMap.put(CONTACT_CUSTOM_FIELD_4, fieldRepository.findByName(CONTACT_CUSTOM_FIELD_4).orElseThrow());
        fieldMap.put(CONTACT_CUSTOM_FIELD_5, fieldRepository.findByName(CONTACT_CUSTOM_FIELD_5).orElseThrow());
        fieldMap.put(CONTACT_CUSTOM_FIELD_6, fieldRepository.findByName(CONTACT_CUSTOM_FIELD_6).orElseThrow());

        fieldMap.put(CONTACT_CUSTOM_LIST_1, fieldRepository.findByName(CONTACT_CUSTOM_LIST_1).orElseThrow());
        fieldMap.put(CONTACT_CUSTOM_LIST_2, fieldRepository.findByName(CONTACT_CUSTOM_LIST_2).orElseThrow());
        fieldMap.put(CONTACT_CUSTOM_LIST_3, fieldRepository.findByName(CONTACT_CUSTOM_LIST_3).orElseThrow());
        fieldMap.put(CONTACT_CUSTOM_LIST_4, fieldRepository.findByName(CONTACT_CUSTOM_LIST_4).orElseThrow());
        fieldMap.put(CONTACT_CUSTOM_LIST_5, fieldRepository.findByName(CONTACT_CUSTOM_LIST_5).orElseThrow());
        fieldMap.put(CONTACT_CUSTOM_LIST_6, fieldRepository.findByName(CONTACT_CUSTOM_LIST_6).orElseThrow());

        fieldMap.put(JOB_CUSTOM_FIELD_1, fieldRepository.findByName(JOB_CUSTOM_FIELD_1).orElseThrow());
        fieldMap.put(JOB_CUSTOM_FIELD_2, fieldRepository.findByName(JOB_CUSTOM_FIELD_2).orElseThrow());
        fieldMap.put(JOB_CUSTOM_FIELD_3, fieldRepository.findByName(JOB_CUSTOM_FIELD_3).orElseThrow());
        fieldMap.put(JOB_CUSTOM_FIELD_4, fieldRepository.findByName(JOB_CUSTOM_FIELD_4).orElseThrow());
        fieldMap.put(JOB_CUSTOM_FIELD_5, fieldRepository.findByName(JOB_CUSTOM_FIELD_5).orElseThrow());
        fieldMap.put(JOB_CUSTOM_FIELD_6, fieldRepository.findByName(JOB_CUSTOM_FIELD_6).orElseThrow());
        fieldMap.put(JOB_CUSTOM_LIST_1, fieldRepository.findByName(JOB_CUSTOM_LIST_1).orElseThrow());
        fieldMap.put(JOB_CUSTOM_LIST_2, fieldRepository.findByName(JOB_CUSTOM_LIST_2).orElseThrow());
        fieldMap.put(JOB_CUSTOM_LIST_3, fieldRepository.findByName(JOB_CUSTOM_LIST_3).orElseThrow());
        fieldMap.put(JOB_CUSTOM_LIST_4, fieldRepository.findByName(JOB_CUSTOM_LIST_4).orElseThrow());
        fieldMap.put(JOB_CUSTOM_LIST_5, fieldRepository.findByName(JOB_CUSTOM_LIST_5).orElseThrow());
        fieldMap.put(JOB_CUSTOM_LIST_6, fieldRepository.findByName(JOB_CUSTOM_LIST_6).orElseThrow());

    }

    private void migrateFields(String path) {
        try (DBFReader reader =
                     dbfReaderFactory.createReader(path + "/ctfields.dbf", null)) {
            updateFields(reader);
        } catch (Exception e) {
            log.info("There is error occurred while parsing ctfields.dbf: {}", e.getLocalizedMessage());
        }
    }

    private void updateFields(DBFReader reader) {
        DBFRow row;
        while ((row = reader.nextRow()) != null) {
            try {
                FieldDto fieldDto = FieldDto.builder()
                        .name(row.getString("FI_VARNAME"))
                        .defaultValue(row.getString("FI_NAME"))
                        .alias(row.getString("FI_ALIAS"))
                        .enabled(row.getBoolean("FI_RVBLIST"))
                        .order((short) row.getInt("FI_RVBORDR"))
                        .build();
                fieldService.updateField(fieldDto);
            } catch (Exception e) {
                log.info("There is error occurred while parsing fields info: {}", e.getLocalizedMessage());
            }
        }
    }

    private List<String> getEmployeeColumns() {
        return List.of("id", "name", "password", NOTES, "color", "role", IS_DELETED);
    }

    private void processRows(DBFReader reader, EntityType type, List<String> columns) {
        DBFRow row;
        while ((row = reader.nextRow()) != null) {
            switch (type) {
                case EMPLOYEE -> processEmployeeRow(row, columns);
                case TASK -> processTaskRow(row, columns);
                case CONTACT -> processContactRow(row, columns);
                case JOB -> processJobRow(row, columns);
            }
        }
    }

    private void processJobRow(DBFRow row, List<String> columns) {
        List<Object> values = new ArrayList<>();
        String newId = UUID.randomUUID().toString();
        try {
            values.add(newId);
            values.add(row.getDate("JO_CREATED").toString());
            values.add(row.getDate("JO_LASTMOD").toString());

            values.add(row.getString("JO_JOBNO")
                    .replace("'", "''"));
            values.add(row.getString("JO_LOTNO")
                    .replace("'", "''"));
            values.add(row.getString("JO_ADDR")
                    .replace("'", "''"));
            values.add(row.getString("JO_ADDR2")
                    .replace("'", "''"));
            values.add(row.getString("JO_CITY")
                    .replace("'", "''"));
            values.add(row.getString("JO_STATE")
                    .replace("'", "''"));
            values.add(row.getString("JO_ZIP")
                    .replace("'", "''"));
            values.add(row.getString("JO_LOCKBOX")
                    .replace("'", "''"));
            values.add(row.getString("JO_DIRECTI")
                    .replace(NULL, "")
                    .trim()
                    .replace("'", "''"));
            values.add(row.getString("JO_DESC")
                    .replace(NULL, "")
                    .replace("'", "''"));
            values.add(row.getString("JO_DESCRTF")
                    .replace(NULL, "")
                    .replace("'", "''"));
            values.add(row.getString("JO_OWNER")
                    .replace("'", "''"));
            values.add(row.getString("JO_EMAIL")
                    .replace("'", "''"));
            values.add(row.getString("JO_COUNTRY")
                    .replace("'", "''"));
            values.add(row.getString("JO_OWNERPH")
                    .replace("'", "''"));
            values.add(row.getString("JO_WORK1")
                    .replace("'", "''"));
            values.add(row.getString("JO_CELL1")
                    .replace("'", "''"));
            values.add(row.getString("JO_FAX1")
                    .replace("'", "''"));
            values.add(row.getString("JO_COMPANY")
                    .replace("'", "''"));

            setCustomValue(newId, JOB_CUSTOM_FIELD_1,
                    row.getString("JO_CUSTF1").replace("'", "''"));
            setCustomValue(newId, JOB_CUSTOM_FIELD_2,
                    row.getString("JO_CUSTF2").replace("'", "''"));
            setCustomValue(newId, JOB_CUSTOM_FIELD_3,
                    row.getString("JO_CUSTF3").replace("'", "''"));
            setCustomValue(newId, JOB_CUSTOM_FIELD_4,
                    row.getString("JO_CUSTF4").replace("'", "''"));
            setCustomValue(newId, JOB_CUSTOM_FIELD_5,
                    row.getString("JO_CUSTF5").replace("'", "''"));
            setCustomValue(newId, JOB_CUSTOM_FIELD_6,
                    row.getString("JO_CUSTF6").replace("'", "''"));

            setCustomValue(newId, JOB_CUSTOM_LIST_1,
                    row.getString("JO_CUSTL1").replace("'", "''"));
            setCustomValue(newId, JOB_CUSTOM_LIST_2,
                    row.getString("JO_CUSTL2").replace("'", "''"));
            setCustomValue(newId, JOB_CUSTOM_LIST_3,
                    row.getString("JO_CUSTL3").replace("'", "''"));
            setCustomValue(newId, JOB_CUSTOM_LIST_4,
                    row.getString("JO_CUSTL4").replace("'", "''"));
            setCustomValue(newId, JOB_CUSTOM_LIST_5,
                    row.getString("JO_CUSTL5").replace("'", "''"));
            setCustomValue(newId, JOB_CUSTOM_LIST_6,
                    row.getString("JO_CUSTL6").replace("'", "''"));

            values.add(row.isDeleted());

            jobCodes.put(row.getString("JO_JOBNO"), newId);

            dbConnection.addRow(values);

            if (dbConnection.getMultiInsertLength() > 10_000_000) {
                flushDataToDatabase("jobs", columns);
            }

        } catch (Exception e) {
            log.error("Error occurred while parsing job info: {}", e.getLocalizedMessage());
        }
    }

    private void processContactRow(DBFRow row, List<String> columns) {
        List<Object> values = new ArrayList<>();
        String newId = UUID.randomUUID().toString();
        try {
            values.add(newId);
            values.add(row.getDate("CU_ENTERED").toString());
            values.add(row.getDate("CU_LASTMOD").toString());

            values.add(row.getString("CU_NAME").replace("'", "''"));
            values.add(row.getString("CU_PROFESS").replace("'", "''"));
            values.add(row.getString("CU_LAST").replace("'", "''"));
            values.add(row.getString("CU_FIRST").replace("'", "''"));
            values.add(row.getString("CU_COMMENT").replace("'", "''"));
            values.add(row.getString("CU_NOTES")
                    .replace(NULL, "").replace("'", "''"));
            values.add(row.getString("CU_NOTERTF")
                    .replace(NULL, "").replace("'", "''"));
            values.add(row.getString("CU_EMAIL").replace("'", "''"));
            values.add(row.getString("CU_WWW").replace("'", "''"));
            values.add(row.getString("CU_SPOUSE").replace("'", "''"));
            values.add(row.getString("CU_BOSS").replace("'", "''"));
            values.add(row.getString("CU_TAXID").replace("'", "''"));
            values.add(row.getString("CU_FAXNO").replace("'", "''"));
            values.add(
                    row.getDate("CU_COMPDAT") == null ? null :
                            row.getDate("CU_COMPDAT").toString());
            values.add(
                    row.getDate("CU_INSDAT") == null ? null :
                            row.getDate("CU_INSDAT").toString());

            setCustomValue(newId, CONTACT_CUSTOM_FIELD_1,
                    row.getString("CU_CUSTF1").replace("'", "''"));
            setCustomValue(newId, CONTACT_CUSTOM_FIELD_2,
                    row.getString("CU_CUSTF2").replace("'", "''"));
            setCustomValue(newId, CONTACT_CUSTOM_FIELD_3,
                    row.getString("CU_CUSTF3").replace("'", "''"));
            setCustomValue(newId, CONTACT_CUSTOM_FIELD_4,
                    row.getString("CU_CUSTF4").replace("'", "''"));
            setCustomValue(newId, CONTACT_CUSTOM_FIELD_5,
                    row.getString("CU_CUSTF5").replace("'", "''"));
            setCustomValue(newId, CONTACT_CUSTOM_FIELD_6,
                    row.getString("CU_CUSTF6").replace("'", "''"));

            setCustomValue(newId, CONTACT_CUSTOM_LIST_1,
                    row.getString("CU_CUSTL1").replace("'", "''"));
            setCustomValue(newId, CONTACT_CUSTOM_LIST_2,
                    row.getString("CU_CUSTL2").replace("'", "''"));
            setCustomValue(newId, CONTACT_CUSTOM_LIST_3,
                    row.getString("CU_CUSTL3").replace("'", "''"));
            setCustomValue(newId, CONTACT_CUSTOM_LIST_4,
                    row.getString("CU_CUSTL4").replace("'", "''"));
            setCustomValue(newId, CONTACT_CUSTOM_LIST_5,
                    row.getString("CU_CUSTL5").replace("'", "''"));
            setCustomValue(newId, CONTACT_CUSTOM_LIST_6,
                    row.getString("CU_CUSTL6").replace("'", "''"));

            values.add(row.isDeleted());
            contactCodes.put(row.getString("CU_CUSTID"), newId);

            dbConnection.addRow(values);

            if (dbConnection.getMultiInsertLength() > 10_000_000) {
                flushDataToDatabase("contacts", columns);
            }
        } catch (Exception e) {
            log.error("Error occurred while parsing contact info: {}", e.getLocalizedMessage());
        }
    }

    private void processTaskRow(DBFRow row, List<String> columns) {
        List<Object> values = new ArrayList<>();

        String newId = UUID.randomUUID().toString();

        try {
            values.add(newId);

            values.add(row.getDate("TA_CREATED").toString());
            values.add(row.getDate("TA_LASTMOD").toString());

            values.add(row.getInt("TA_TASKNO"));
            values.add(jobCodes.get(row.getString("TA_JOBNO")));
            values.add(setDescription(row.getString("TA_TASK")).replace("'", "''"));
            values.add(row.getString("TA_DESC")
                    .replace(NULL, "")
                    .replace("'", "''"));
            values.add(row.getString("TA_DESCRTF")
                    .replace(NULL, "")
                    .replace("'", "''"));
            values.add(contactCodes.get(row.getString("TA_CUSTID")));
            values.add(
                    row.getDate("TA_DTARGET") == null ? null :
                            row.getDate("TA_DTARGET").toString());
            values.add(
                    row.getDate("TA_DDONE") == null ? null :
                            row.getDate("TA_DDONE").toString());
            values.add(
                    row.getDate("TA_FTARGET") == null ? null :
                            row.getDate("TA_FTARGET").toString());
            values.add(row.getString("TA_ORDER").replace("'", "''"));
            values.add(row.getString("TA_STATUS"));
            values.add(row.getString("TA_DAYS"));
            values.add(row.getString("TA_ISACT"));
            values.add(employeeCodes.get(row.getString("TA_REQUEST")));

            setCustomValue(newId, TASK_CUSTOM_FIELD_1,
                    row.getString("TA_CUSTF1").replace("'", "''"));
            setCustomValue(newId, TASK_CUSTOM_FIELD_2,
                    row.getString("TA_CUSTF2").replace("'", "''"));
            setCustomValue(newId, TASK_CUSTOM_FIELD_3,
                    row.getString("TA_CUSTF3").replace("'", "''"));
            setCustomValue(newId, TASK_CUSTOM_FIELD_4,
                    row.getString("TA_CUSTF4").replace("'", "''"));
            setCustomValue(newId, TASK_CUSTOM_FIELD_5,
                    row.getString("TA_CUSTF5").replace("'", "''"));
            setCustomValue(newId, TASK_CUSTOM_FIELD_6,
                    row.getString("TA_CUSTF6").replace("'", "''"));

            setCustomValue(newId, TASK_CUSTOM_LIST_1,
                    row.getString("TA_CUSTL1").replace("'", "''"));
            setCustomValue(newId, TASK_CUSTOM_LIST_2,
                    row.getString("TA_CUSTL2").replace("'", "''"));
            setCustomValue(newId, TASK_CUSTOM_LIST_3,
                    row.getString("TA_CUSTL3").replace("'", "''"));
            setCustomValue(newId, TASK_CUSTOM_LIST_4,
                    row.getString("TA_CUSTL4").replace("'", "''"));
            setCustomValue(newId, TASK_CUSTOM_LIST_5,
                    row.getString("TA_CUSTL5").replace("'", "''"));
            setCustomValue(newId, TASK_CUSTOM_LIST_6,
                    row.getString("TA_CUSTL6").replace("'", "''"));

            values.add(row.isDeleted());

            taskCodes.put(row.getInt("TA_TASKNO"), newId);

            pendingTasks.put(newId, row.getInt("TA_PENDING"));

            dbConnection.addRow(values);

            if (dbConnection.getMultiInsertLength() > 10_000_000) {
                flushDataToDatabase("tasks", columns);
            }
        } catch (Exception e) {
            log.error("Error occurred while parsing task info: {}", e.getLocalizedMessage());
        }
    }

    private void setCustomValue(String entityId, String fieldName, String customValue) {
        if (customValue.isBlank()) return;

        Field field = fieldMap.get(fieldName);

        FieldValue findValue = fieldValueRepository.findByFieldAndValue(field, customValue)
                .orElseGet(() -> FieldValue.builder()
                        .value(customValue).field(field).build());
        FieldValue fieldValue = fieldValueRepository.save(findValue);

        entityCustomValues.put(entityId, fieldValue.getId());

    }

    private void processEmployeeRow(DBFRow row, List<String> columns) {
        List<Object> values = new ArrayList<>();
        String newId = UUID.randomUUID().toString();
        try {
            values.add(newId);
            values.add(row.getString("EM_NAME").replace("'", "''"));
            values.add(row.getString("EM_PWORD").replace("'", "''"));
            values.add(row.getString("EM_NOTES")
                    .replace(NULL, "")
                    .replace("'", "''"));
            values.add(row.getString("EM_COLOR"));
            values.add(row.getString("EM_RIGHTS"));
            values.add(row.isDeleted());

            employeeCodes.put(row.getString("EM_NAME"), newId);

            dbConnection.addRow(values);

            if (dbConnection.getMultiInsertLength() > 10_000_000) {
                flushDataToDatabase(EntityType.EMPLOYEE + "s", columns);
            }
        } catch (Exception e) {
            log.error("Error occurred while parsing employee info: {}", e.getLocalizedMessage());
        }
    }

    private void flushDataToDatabase(String table, List<String> columns) {
        try {
            dbConnection.executeMultiInsert(table, columns);
            dbConnection.resetMultiInsert();
        } catch (SQLException e) {
            log.error("Error occurred while inserting data into table: {}", e.getLocalizedMessage());
        }
    }

    private void migrateEmployees(String path) {
        DBFReader reader = null;
        List<String> columns = getEmployeeColumns();
        try {
            reader = dbfReaderFactory.createReader(path + "/ctemp.dbf", path + "/ctemp.fpt");
            processRows(reader, EntityType.EMPLOYEE, columns);

        } catch (Exception e) {
            log.error("Error occurred while parsing ctemp.dbf: {}", e.getLocalizedMessage());
        } finally {
            closeResources(reader, "employees", columns);
        }
    }

    private void closeResources(DBFReader reader, String table, List<String> columns) {
        try {
            flushDataToDatabase(table, columns);
        } catch (Exception e) {
            log.error("Error occurred while finalizing data insertion into {}: {}",
                    table, e.getLocalizedMessage());
        } finally {
            DBFUtils.close(reader);
        }
    }


    public void migrateTasks(String path) {
        DBFReader reader = null;
        List<String> columns = getTaskColumns();
        try {
            reader = dbfReaderFactory.createReader(path + "/tmtask.dbf", path + "/tmtask.fpt");
            processRows(reader, EntityType.TASK, columns);
        } catch (Exception e) {
            log.error("Error occurred while parsing tmtask.dbf: {}", e.getLocalizedMessage());
        } finally {
            closeResources(reader, "tasks", columns);
            dbConnection.updateTasksNumberSequence();
        }
    }


    private List<String> getTaskColumns() {
        return List.of("id", CREATED_TIME, MODIFIED_TIME, "number", "job_id",
                "description", NOTES, NOTES_RTF, "contact_id", "target_start", "actual_finish", "target_finish",
                "\"order\"", "status", "duration", "marked", "requested_id",
                IS_DELETED);
    }

    private String setDescription(String taTask) {
        return taTask == null ? "~Empty description~" : taTask;
    }

    public void migrateContacts(String path) {
        DBFReader reader = null;
        List<String> columns = getContactColumns();
        try {
            reader = dbfReaderFactory.createReader(path + "/ctcust.dbf", path + "/ctcust.fpt");
            processRows(reader, EntityType.CONTACT, columns);
        } catch (Exception e) {
            log.error("Error occurred while parsing ctcust.dbf: {}", e.getLocalizedMessage());
        } finally {
            closeResources(reader, "contacts", columns);
        }
    }

    private List<String> getContactColumns() {
        return List.of("id", CREATED_TIME, MODIFIED_TIME, COMPANY, "profession", "last_name", "first_name",
                "comments", NOTES, NOTES_RTF, "email", "web_site", "spouse", "supervisor", "tax_id", "fax",
                "workers_comp_date", "insurance_date", IS_DELETED);
    }

    public void migrateJobs(String path) {
        DBFReader reader = null;
        List<String> columns = getJobColumns();

        try {
            reader = dbfReaderFactory.createReader(path + "/tmjob.dbf", path + "/tmjob.fpt");
            processRows(reader, EntityType.JOB, columns);

        } catch (Exception e) {
            log.error("Error occurred while parsing tmjob.dbf: {}", e.getLocalizedMessage());
        } finally {
            closeResources(reader, "jobs", columns);
        }
    }

    private List<String> getJobColumns() {
        return List.of("id", CREATED_TIME, MODIFIED_TIME, "number", "lot", "address1", "address2",
                "city", "state", "postal", "lock_box", "directions", NOTES, NOTES_RTF, "owner_name", "email",
                "country", "home_phone", "work_phone", "cell_phone", "fax", COMPANY, IS_DELETED);
    }
}
