package net.virtualboss.service;

import com.linuxense.javadbf.DBFReader;
import com.linuxense.javadbf.DBFRow;
import com.linuxense.javadbf.DBFUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.repository.ContactRepository;
import net.virtualboss.repository.JobRepository;
import net.virtualboss.repository.TaskRepository;
import net.virtualboss.util.DBConnection;
import org.springframework.stereotype.Service;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class MigrationService {
    private final ContactRepository contactRepository;
    private final TaskRepository taskRepository;
    private final JobRepository jobRepository;

    private final Map<String, String> jobCodes = new HashMap<>();
    private final Map<String, String> contactCodes = new HashMap<>();
    private final Map<Integer, String> taskCodes = new HashMap<>();
    private final Map<String, Integer> pendingTasks = new HashMap<>();

    public void migrate(String dataPath) {

        if (dataPath == null) {
            dataPath = "db/sampleJobs/SampleCompanyInc";
        }

        migrateJobs(dataPath);
        migrateContacts(dataPath);
        migrateTasks(dataPath);
    }

    public void migrateTasks(String path) {
        DBFReader reader = null;
        List<String> columns = getTaskColumns();

        try {
            reader = new DBFReader(new FileInputStream(path + "/tmtask.dbf"));
            reader.setMemoFile(new File(path + "/tmtask.fpt"));

            DBFRow row;
            while ((row = reader.nextRow()) != null) {

                List<Object> values = new ArrayList<>();

                String newId = UUID.randomUUID().toString();

                try {
                    values.add(newId);
                    values.add(jobCodes.get(row.getString("TA_JOBNO")));
                    values.add(setDescription(row.getString("TA_TASK")).replace("'", "''"));
                    values.add(row.getString("TA_DESC")
                            .replace("\u0000", "")
                            .replace("'", "''"));
                    values.add(row.getString("TA_DESCRTF")
                            .replace("\u0000", "")
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
//                        task.setOrder(rec.getInteger("TA_TASKNO"));
                    values.add(row.getString("TA_STATUS").replace("'", "''"));
                    values.add(row.getString("TA_DAYS"));

                    values.add(row.isDeleted());

                } catch (Exception e) {
                    log.info("There is error occurred while parsing task info: {}", e.getLocalizedMessage());
                }

                taskCodes.put(row.getInt("TA_TASKNO"), newId);

                DBConnection.addRow(values);
                if (DBConnection.multiInsert.length() > 10_000_000) {
                    try {
                        DBConnection.executeMultiInsert("tasks", columns);
                    } catch (SQLException e) {
                        log.info("There is error occurred while inserting data into db from tmtask.dbf : {}", e.getLocalizedMessage());
                    }
                }
//                    pendingTasks.put(saved.getId().toString(), row.getInt("TA_PENDING"));
            }

        } catch (Exception e) {
            log.info("There is error occurred while parsing tmtask.dbf: {}", e.getLocalizedMessage());
        } finally {
            try {
                DBConnection.executeMultiInsert("tasks", columns);
            } catch (SQLException e) {
                log.info("There is error occurred while inserting data into db from tmtask.dbf : {}", e.getLocalizedMessage());
            }
            DBFUtils.close(reader);
        }
    }

    private List<String> getTaskColumns() {
        List<String> columns = new ArrayList<>();

        columns.add("id");
        columns.add("job_id");
        columns.add("description");
        columns.add("notes");
        columns.add("notes_rtf");
        columns.add("contact_id");
        columns.add("target_start");
        columns.add("actual_finish");
        columns.add("target_finish");
        columns.add("\"order\"");
        columns.add("status");
        columns.add("duration");

        columns.add("is_deleted");

        return columns;
    }

    private String setDescription(String taTask) {
        return taTask == null ? "~Empty description~" : taTask;
    }

    public void migrateContacts(String path) {

        DBFReader reader = null;

        List<String> columns = getContactColumns();

        try {
            reader = new DBFReader(new FileInputStream(path + "/ctcust.dbf"));
            reader.setMemoFile(new File(path + "/ctcust.fpt"));

            DBFRow row;
            while ((row = reader.nextRow()) != null) {
                List<Object> values = new ArrayList<>();

                String newId = UUID.randomUUID().toString();

                try {
                    values.add(newId);

                    values.add(row.getString("CU_NAME").replace("'", "''"));
                    values.add(row.getString("CU_PROFESS").replace("'", "''"));
                    values.add(row.getString("CU_LAST").replace("'", "''"));
                    values.add(row.getString("CU_FIRST").replace("'", "''"));
                    values.add(row.getString("CU_COMMENT").replace("'", "''"));
                    values.add(row.getString("CU_NOTES")
                            .replace("\u0000", "").replace("'", "''"));
                    values.add(row.getString("CU_NOTERTF")
                            .replace("\u0000", "").replace("'", "''"));
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
                    values.add(row.isDeleted());

                } catch (Exception e) {
                    log.info("There is error occurred while parsing contact info: {}", e.getLocalizedMessage());
                }
                DBConnection.addRow(values);
                if (DBConnection.multiInsert.length() > 10_000_000) {
                    try {
                        DBConnection.executeMultiInsert("contacts", columns);
                    } catch (SQLException e) {
                        log.info("There is error occurred while inserting data into db from ctcust.dbf : {}", e.getLocalizedMessage());
                    }
                }
                contactCodes.put(row.getString("CU_CUSTID"), newId);
            }
        } catch (Exception e) {
            log.info("There is error occurred while parsing ctcust.dbf: {}", e.getLocalizedMessage());
        } finally {
            try {
                DBConnection.executeMultiInsert("contacts", columns);
            } catch (SQLException e) {
                log.info("There is error occurred while inserting data into db from ctcust.dbf : {}", e.getLocalizedMessage());
            }
            DBFUtils.close(reader);
        }
    }

    private List<String> getContactColumns() {
        List<String> columns = new ArrayList<>();
        columns.add("id");
        columns.add("company");
        columns.add("profession");
        columns.add("last_name");
        columns.add("first_name");
        columns.add("comments");
        columns.add("notes");
        columns.add("notes_rtf");
        columns.add("email");
        columns.add("web_site");
        columns.add("spouse");
        columns.add("supervisor");
        columns.add("tax_id");
        columns.add("fax");
        columns.add("workers_comp_date");
        columns.add("insurance_date");
        columns.add("is_deleted");
        return columns;
    }

    public void migrateJobs(String path) {

        DBFReader reader = null;

        List<String> columns = getJobColumns();

        try {
            reader = new DBFReader(new FileInputStream(path + "/tmjob.dbf"));
            reader.setMemoFile(new File(path + "/tmjob.fpt"));

            DBFRow row;
            while ((row = reader.nextRow()) != null) {
                List<Object> values = new ArrayList<>();

                String newId = UUID.randomUUID().toString();

                try {
                    values.add(newId);
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
                            .replace("\u0000", "")
                            .trim()
                            .replace("'", "''"));
                    values.add(row.getString("JO_DESC")
                            .replace("\u0000", "")
                            .replace("'", "''"));
                    values.add(row.getString("JO_DESCRTF")
                            .replace("\u0000", "")
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
                    values.add(row.isDeleted());

                } catch (Exception e) {
                    log.info("There is error occurred while parsing job info: {}", e.getLocalizedMessage());
                }

                DBConnection.addRow(values);
                if (DBConnection.multiInsert.length() > 10_000_000) {
                    try {
                        DBConnection.executeMultiInsert("jobs", columns);
                    } catch (SQLException e) {
                        log.info("There is error occurred while inserting data into db from tmjob.dbf : {}", e.getLocalizedMessage());
                    }
                }

                jobCodes.put(row.getString("JO_JOBNO"), newId);
            }
        } catch (Exception e) {
            log.info("There is error occurred while parsing tmjob.dbf : {}", e.getLocalizedMessage());
        } finally {
            try {
                DBConnection.executeMultiInsert("jobs", columns);
            } catch (SQLException e) {
                log.info("There is error occurred while inserting data into db from tmjob.dbf : {}", e.getLocalizedMessage());
            }
            DBFUtils.close(reader);
        }
    }

    private List<String> getJobColumns() {
        List<String> columns = new ArrayList<>();

        columns.add("id");
        columns.add("number");
        columns.add("lot");
        columns.add("address1");
        columns.add("address2");
        columns.add("city");
        columns.add("state");
        columns.add("postal");
        columns.add("lock_box");
        columns.add("directions");
        columns.add("notes");
        columns.add("notes_rtf");
        columns.add("owner_name");
        columns.add("email");
        columns.add("country");
        columns.add("home_phone");
        columns.add("work_phone");
        columns.add("cell_phone");
        columns.add("fax");
        columns.add("company");
        columns.add("is_deleted");
        return columns;
    }
}
