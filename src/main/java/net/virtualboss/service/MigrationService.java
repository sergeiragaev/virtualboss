package net.virtualboss.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.jdbf.core.DbfField;
import net.jdbf.core.DbfMetadata;
import net.jdbf.core.DbfRecord;
import net.jdbf.reader.DbfReader;
import net.virtualboss.model.entity.Contact;
import net.virtualboss.model.entity.Job;
import net.virtualboss.model.entity.Task;
import net.virtualboss.repository.ContactRepository;
import net.virtualboss.repository.JobRepository;
import net.virtualboss.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    private final Charset stringCharset = Charset.forName("cp1252");

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd").localizedBy(Locale.US);

    private final List<String> samples = new ArrayList<>();

    public void migrate() {

//        samples.add("db/sampleJobs/HomeBuildingSample01");
//        samples.add("db/sampleJobs/HomeBuildingSample02");
//        samples.add("db/sampleJobs/HomeBuildingSample03");
//        samples.add("db/sampleJobs/HomeBuildingSample04");
//        samples.add("db/sampleJobs/HomeBuildingSample05");
//        samples.add("db/sampleJobs/HomeBuildingSample06");
//        samples.add("db/sampleJobs/HomeBuildingSample07");
//        samples.add("db/sampleJobs/HomeBuildingSample08");
//        samples.add("db/sampleJobs/HomeBuildingSample09");
//        samples.add("db/sampleJobs/HomeBuildingSample10");
//        samples.add("db/sampleJobs/HomeBuildingSample11");
//        samples.add("db/sampleJobs/HomeBuildingSample12");
//        samples.add("db/sampleJobs/HomeBuildingSample13");
//        samples.add("db/sampleJobs/Landscaping Samples");
//        samples.add("db/sampleJobs/PoolSample");
//        samples.add("db/sampleJobs/RemodelingSamples");
//        samples.add("db/sampleJobs/ResidentialElectricSample");
        samples.add("db/sampleJobs/SampleCompanyInc");

        for (String path : samples) {
            migrateJobs(path);
            migrateContacts(path);
            migrateTasks(path, 0, 0);
        }
    }

    public void migrateTasks(String path, int plusYears, int plusMonths) {

        InputStream dbf = getClass().getClassLoader().getResourceAsStream(path + "/tmtask.dbf");
        InputStream memo = getClass().getClassLoader().getResourceAsStream(path + "/tmtask.fpt");

        try (DbfReader reader = new DbfReader(dbf, memo)) {
            DbfMetadata meta = reader.getMetadata();
            System.out.println("Read tmtask DBF Metadata: " + meta);

            DbfRecord rec;
            while ((rec = reader.read()) != null) {
                if (rec.isDeleted()) continue;

                rec.setStringCharset(stringCharset);

                Task task = new Task();

                for (DbfField field : meta.getFields()) {
                    String name = field.getName();
                    switch (name) {
                        case "TA_JOBNO" -> task.setJob(
                                jobRepository.findById(UUID.fromString(
                                        jobCodes.get(rec.getString("TA_JOBNO")))).orElseThrow());
                        case "TA_TASK" -> task.setDescription(rec.getString("TA_TASK"));
                        case "TA_DESC" -> task.setNotes(rec.getMemoAsString("TA_DESC"));
                        case "TA_CUSTID" -> task.setContact(
                                contactRepository.findById(UUID.fromString(
                                        contactCodes.get(rec.getString("TA_CUSTID")))).orElseThrow());
                        case "TA_DTARGET" -> task.setTargetStart(
                                rec.getString("TA_DTARGET") == null ?
                                        null : LocalDate.parse(rec.getString("TA_DTARGET"), formatter));
                        case "TA_DDONE" -> task.setActualFinish(
                                rec.getString("TA_DDONE") == null ?
                                        null : LocalDate.parse(rec.getString("TA_DDONE"), formatter));
                        case "TA_FTARGET" -> task.setTargetFinish(
                                rec.getString("TA_FTARGET") == null ?
                                        null : LocalDate.parse(rec.getString("TA_FTARGET"), formatter));
                        case "TA_ORDER" -> task.setOrder(rec.getString("TA_ORDER"));
//                        case "TA_TASKNO" -> task.setOrder(rec.getInteger("TA_TASKNO"));
                        case "TA_STATUS" -> task.setStatus(rec.getString("TA_STATUS"));
                        case "TA_DAYS" -> task.setDuration(Short.parseShort(rec.getInteger("TA_DAYS").toString()));
                    }
                }

                Task saved = taskRepository.save(task);

                taskCodes.put(rec.getInteger("TA_TASKNO"), saved.getId().toString());
                pendingTasks.put(saved.getId().toString(), rec.getInteger("TA_PENDING"));
            }


            List<Task> tasks = taskRepository.findAll();
            for (Task task : tasks) {
                task.setTargetStart(task.getTargetStart().plusYears(plusYears).plusMonths(plusMonths));
                task.setTargetFinish(task.getTargetFinish().plusYears(plusYears).plusMonths(plusMonths));
                taskRepository.save(task);
            }

//            for (Map.Entry<String, Integer> entry : pendingTasks.entrySet()) {
//                Task task = taskRepository.findById(UUID.fromString(entry.getKey())).orElseThrow();
//                UUID pendingTaskCode = UUID.fromString(taskCodes.get(entry.getValue()));
//                task.getFollows().add(taskRepository.findById(pendingTaskCode).orElseThrow());
//                taskRepository.save(task);
//            }

        } catch (IOException e) {
            //e.printStackTrace();
//        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void migrateContacts(String path) {

        InputStream dbf = getClass().getClassLoader().getResourceAsStream(path + "/ctcust.dbf");
        InputStream memo = getClass().getClassLoader().getResourceAsStream(path + "/ctcust.fpt");

        try (DbfReader reader = new DbfReader(dbf, memo)) {
            DbfMetadata meta = reader.getMetadata();
            System.out.println("Read ctcust DBF Metadata: " + meta);

            DbfRecord rec;
            while ((rec = reader.read()) != null) {
                if (rec.isDeleted()) continue;

                rec.setStringCharset(stringCharset);

                Contact contact = new Contact();

                for (DbfField field : meta.getFields()) {
                    String name = field.getName();
                    switch (name) {
                        case "CU_NAME" -> contact.setCompany(rec.getString("CU_NAME"));
                        case "CU_PROFESS" -> contact.setProfession(rec.getString("CU_PROFESS"));
                        case "CU_LAST" -> contact.setLastName(rec.getString("CU_LAST"));
                        case "CU_FIRST" -> contact.setFirstName(rec.getString("CU_FIRST"));
                        case "CU_COMMENT" -> contact.setComments(rec.getString("CU_COMMENT"));
                        case "CU_NOTES" -> contact.setNotes(rec.getMemoAsString("CU_NOTES"));
                        case "CU_EMAIL" -> contact.setEmail(rec.getString("CU_EMAIL"));
                        case "CU_WWW" -> contact.setWebSite(rec.getString("CU_WWW"));
                        case "CU_SPOUSE" -> contact.setSpouse(rec.getString("CU_SPOUSE"));
                        case "CU_BOSS" -> contact.setSupervisor(rec.getString("CU_BOSS"));
                        case "CU_TAXID" -> contact.setTaxId(rec.getString("CU_TAXID"));
                        case "CU_FAXNO" -> contact.setFax(rec.getString("CU_FAXNO"));
                        case "CU_COMPDAT" -> contact.setWorkersCompDate(
                                rec.getString("CU_COMPDAT") == null ?
                                null : LocalDate.parse(rec.getString("CU_COMPDAT"), formatter));
                        case "CU_INSDAT" -> contact.setInsuranceDate(
                                rec.getString("CU_INSDAT") == null ?
                                        null : LocalDate.parse(rec.getString("CU_INSDAT"), formatter));
                    }
                }

                Contact saved = contactRepository.save(contact);

                contactCodes.put(rec.getString("CU_CUSTID"), saved.getId().toString());

            }

        } catch (IOException e) {
            //e.printStackTrace();
//        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void migrateJobs(String path) {

        InputStream dbf = getClass().getClassLoader().getResourceAsStream(path + "/tmjob.dbf");
        InputStream memo = getClass().getClassLoader().getResourceAsStream(path + "/tmjob.fpt");

        try (DbfReader reader = new DbfReader(dbf, memo)) {
            DbfMetadata meta = reader.getMetadata();
            System.out.println("Read tmjob DBF Metadata: " + meta);

            DbfRecord rec;
            while ((rec = reader.read()) != null) {
                if (rec.isDeleted()) continue;

                rec.setStringCharset(stringCharset);

                Job job = new Job();

                for (DbfField field : meta.getFields()) {
                    String name = field.getName();
                    switch (name) {
                        case "JO_JOBNO" -> job.setNumber(rec.getString("JO_JOBNO"));
                        case "JO_LOTNO" -> job.setLot(rec.getString("JO_LOTNO"));
                        case "JO_ADDR" -> job.setAddress1(rec.getString("JO_ADDR"));
                        case "JO_ADDR2" -> job.setAddress2(rec.getString("JO_ADDR2"));
                        case "JO_CITY" -> job.setCity(rec.getString("JO_CITY"));
                        case "JO_STATE" -> job.setState(rec.getString("JO_STATE"));
                        case "JO_ZIP" -> job.setPostal(rec.getString("JO_ZIP"));
                        case "JO_LOCKBOX" -> job.setLockBox(rec.getString("JO_LOCKBOX"));
                        case "JO_DIRECTI" -> job.setDirections(rec.getString("JO_DIRECTI"));
                        case "JO_DESC" -> job.setNotes(rec.getMemoAsString("JO_DESC"));
                        case "JO_OWNER" -> job.setOwnerName(rec.getString("JO_OWNER"));
                        case "JO_EMAIL" -> job.setEmail(rec.getString("JO_EMAIL"));
                        case "JO_COUNTRY" -> job.setCountry(rec.getString("JO_COUNTRY"));
                        case "JO_OWNERPH" -> job.setOwnerName(rec.getString("JO_OWNERPH"));
                        case "JO_WORK1" -> job.setWorkPhone(rec.getString("JO_WORK1"));
                        case "JO_CELL1" -> job.setCellPhone(rec.getString("JO_CELL1"));
                        case "JO_FAX1" -> job.setFax(rec.getString("JO_FAX1"));
                        case "JO_COMPANY" -> job.setCompany(rec.getString("JO_COMPANY"));
                    }
                }

                Job saved = jobRepository.save(job);

                jobCodes.put(rec.getString("JO_JOBNO"), saved.getId().toString());

            }

        } catch (IOException e) {
            //e.printStackTrace();
//        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
