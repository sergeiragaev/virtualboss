DO $$

DECLARE
field_1 CONSTANT VARCHAR(8) := 'Field #1';
field_2 CONSTANT VARCHAR(8) := 'Field #2';
field_3 CONSTANT VARCHAR(8) := 'Field #3';
field_4 CONSTANT VARCHAR(8) := 'Field #4';
field_5 CONSTANT VARCHAR(8) := 'Field #5';
field_6 CONSTANT VARCHAR(8) := 'Field #6';
list_1 CONSTANT VARCHAR(7) := 'List #1';
list_2 CONSTANT VARCHAR(7) := 'List #2';
list_3 CONSTANT VARCHAR(7) := 'List #3';
list_4 CONSTANT VARCHAR(7) := 'List #4';
list_5 CONSTANT VARCHAR(7) := 'List #5';
list_6 CONSTANT VARCHAR(7) := 'List #6';

BEGIN

insert into fields (id, name, default_value, alias, enabled, "order") values (1, 'TaskNumber', 'Task ID', 'Task ID', true, 1);
insert into fields (id, name, default_value, alias, enabled, "order") values (2, 'TaskDescription', 'Task Description', 'Task Description', true, 2);
insert into fields (id, name, default_value, alias, enabled, "order") values (3, 'TaskTargetStart', 'Target Start', 'Target Start', true, 3);
insert into fields (id, name, default_value, alias, enabled, "order") values (4, 'TaskDuration', 'Duration', 'Duration', true, 4);
insert into fields (id, name, default_value, alias, enabled, "order") values (5, 'TaskTargetFinish', 'Target Finish', 'Target Finish', true, 5);
insert into fields (id, name, default_value, alias, enabled, "order") values (6, 'TaskActualFinish', 'Actual Finish', 'Actual Finish', false, 6);
insert into fields (id, name, default_value, alias, enabled, "order") values (7, 'TaskOrder', 'Task #', 'Task #', false, 7);
insert into fields (id, name, default_value, alias, enabled, "order") values (8, 'TaskStatus', 'Status', 'Status', false, 8);
insert into fields (id, name, default_value, alias, enabled, "order") values (9, 'TaskFollows', 'Follows Task', 'Follows Task', false, 9);
insert into fields (id, name, default_value, alias, enabled, "order") values (10, 'TaskRequested', 'Requested by', 'Requested by', false, 10);
insert into fields (id, name, default_value, alias, enabled, "order") values (11, 'TaskNotes', 'Detailed Task Notes', 'Detailed Task Notes', false, 11);
insert into fields (id, name, default_value, alias, enabled, "order") values (12, 'TaskMarked', 'Marked', 'Marked', false, 12);

insert into fields (id, name, default_value, alias, enabled, "order") values (13, 'JobNumber', 'Job #', 'Job #', true, 1);
insert into fields (id, name, default_value, alias, enabled, "order") values (14, 'JobLot', 'Lot #', 'Lot #', true, 2);
insert into fields (id, name, default_value, alias, enabled, "order") values (15, 'JobSubdivision', 'Subdivision', 'Subdivision', true, 3);
insert into fields (id, name, default_value, alias, enabled, "order") values (16, 'JobOwnerName', 'Owner''s Name', 'Owner''s Name', true, 4);
insert into fields (id, name, default_value, alias, enabled, "order") values (17, 'JobLockBox', 'Lock Box Combo', 'Lock Box Combo', true, 5);
insert into fields (id, name, default_value, alias, enabled, "order") values (18, 'JobDirections', 'Directions to Job Site', 'Directions to Job Site', true, 6);
insert into fields (id, name, default_value, alias, enabled, "order") values (19, 'JobNotes', 'Job Site Notes', 'Job Site Notes', true, 7);
insert into fields (id, name, default_value, alias, enabled, "order") values (20, 'JobAddress1', 'Address', 'Address', true, 8);
insert into fields (id, name, default_value, alias, enabled, "order") values (21, 'JobAddress2', 'Address 2', 'Address 2', true, 9);
insert into fields (id, name, default_value, alias, enabled, "order") values (22, 'JobCity', 'City', 'City', true, 10);
insert into fields (id, name, default_value, alias, enabled, "order") values (23, 'JobState', 'State', 'State', true, 11);
insert into fields (id, name, default_value, alias, enabled, "order") values (24, 'JobPostal', 'Postal', 'Postal', true, 12);
insert into fields (id, name, default_value, alias, enabled, "order") values (25, 'JobHomePhone', 'Owner''s Home#', 'Owner''s Home#', true, 13);
insert into fields (id, name, default_value, alias, enabled, "order") values (26, 'JobWorkPhone', 'Work #', 'Work #', true, 14);
insert into fields (id, name, default_value, alias, enabled, "order") values (27, 'JobCellPhone', 'Cell #', 'Cell #', true, 15);
insert into fields (id, name, default_value, alias, enabled, "order") values (28, 'JobFax', 'Fax number', 'Fax number', true, 16);
insert into fields (id, name, default_value, alias, enabled, "order") values (29, 'JobCompany', 'Company Name', 'Company Name', true, 17);
insert into fields (id, name, default_value, alias, enabled, "order") values (30, 'JobEmail', 'Email', 'Email', true, 18);
insert into fields (id, name, default_value, alias, enabled, "order") values (31, 'JobCountry', 'Country/Region', 'Country/Region', true, 19);

insert into fields (id, name, default_value, alias, enabled, "order") values (32, 'ContactCompany', 'Company', 'Company', true, 1);
insert into fields (id, name, default_value, alias, enabled, "order") values (33, 'ContactProfession', 'Profession', 'Profession', true, 2);
insert into fields (id, name, default_value, alias, enabled, "order") values (34, 'ContactPerson', 'Person''s Name', 'Person''s Name', true, 3);
insert into fields (id, name, default_value, alias, enabled, "order") values (35, 'ContactLastName', 'Last Name', 'Last Name', true, 4);
insert into fields (id, name, default_value, alias, enabled, "order") values (36, 'ContactFirstName', 'First', 'First', true, 5);
insert into fields (id, name, default_value, alias, enabled, "order") values (37, 'ContactSupervisor', 'Supervisor', 'Supervisor', true, 6);
insert into fields (id, name, default_value, alias, enabled, "order") values (38, 'ContactSpouse', 'Spouse', 'Spouse', true, 7);
insert into fields (id, name, default_value, alias, enabled, "order") values (39, 'ContactTaxID', 'Tax ID', 'Tax ID', true, 8);
insert into fields (id, name, default_value, alias, enabled, "order") values (40, 'ContactWebSite', 'Web Site', 'Web Site', true, 9);
insert into fields (id, name, default_value, alias, enabled, "order") values (41, 'ContactWorkersCompDate', 'Workers Comp good through', 'Workers Comp good through', true, 10);
insert into fields (id, name, default_value, alias, enabled, "order") values (42, 'ContactInsuranceDate', 'Insurance good through', 'Insurance good through', true, 11);
insert into fields (id, name, default_value, alias, enabled, "order") values (43, 'ContactComments', 'Comments', 'Comments', true, 12);
insert into fields (id, name, default_value, alias, enabled, "order") values (44, 'ContactNotes', 'Notes', 'Notes', true, 13);
insert into fields (id, name, default_value, alias, enabled, "order") values (45, 'ContactFax', 'Fax #', 'Fax #', true, 14);
insert into fields (id, name, default_value, alias, enabled, "order") values (46, 'ContactEmail', 'EMail Address', 'EMail Address', true, 15);
insert into fields (id, name, default_value, alias, enabled, "order") values (47, 'ContactPhones', 'Contact phone #s', 'Contact phone #s', true, 16);

insert into fields (id, name, default_value, alias, enabled, "order") values (48, 'TaskCustomField1', field_1, field_1, false, 13);
insert into fields (id, name, default_value, alias, enabled, "order") values (49, 'TaskCustomField2', field_2, field_2, false, 14);
insert into fields (id, name, default_value, alias, enabled, "order") values (50, 'TaskCustomField3', field_3, field_3, false, 15);
insert into fields (id, name, default_value, alias, enabled, "order") values (51, 'TaskCustomField4', field_4, field_4, false, 16);
insert into fields (id, name, default_value, alias, enabled, "order") values (52, 'TaskCustomField5', field_5, field_5, false, 17);
insert into fields (id, name, default_value, alias, enabled, "order") values (53, 'TaskCustomField6', field_6, field_6, false, 18);
insert into fields (id, name, default_value, alias, enabled, "order") values (54, 'TaskCustomList1', list_1, list_1, false, 19);
insert into fields (id, name, default_value, alias, enabled, "order") values (55, 'TaskCustomList2', list_2, list_2, false, 20);
insert into fields (id, name, default_value, alias, enabled, "order") values (56, 'TaskCustomList3', list_3, list_3, false, 21);
insert into fields (id, name, default_value, alias, enabled, "order") values (57, 'TaskCustomList4', list_4, list_4, false, 22);
insert into fields (id, name, default_value, alias, enabled, "order") values (58, 'TaskCustomList5', list_5, list_5, false, 23);
insert into fields (id, name, default_value, alias, enabled, "order") values (59, 'TaskCustomList6', list_6, list_6, false, 24);

insert into fields (id, name, default_value, alias, enabled, "order") values (60, 'JobCustomField1', field_1, field_1, false, 20);
insert into fields (id, name, default_value, alias, enabled, "order") values (61, 'JobCustomField2', field_2, field_2, false, 21);
insert into fields (id, name, default_value, alias, enabled, "order") values (62, 'JobCustomField3', field_3, field_3, false, 22);
insert into fields (id, name, default_value, alias, enabled, "order") values (63, 'JobCustomField4', field_4, field_4, false, 23);
insert into fields (id, name, default_value, alias, enabled, "order") values (64, 'JobCustomField5', field_5, field_5, false, 24);
insert into fields (id, name, default_value, alias, enabled, "order") values (65, 'JobCustomField6', field_6, field_6, false, 25);
insert into fields (id, name, default_value, alias, enabled, "order") values (66, 'JobCustomList1', list_1, list_1, false, 26);
insert into fields (id, name, default_value, alias, enabled, "order") values (67, 'JobCustomList2', list_2, list_2, false, 27);
insert into fields (id, name, default_value, alias, enabled, "order") values (68, 'JobCustomList3', list_3, list_3, false, 28);
insert into fields (id, name, default_value, alias, enabled, "order") values (69, 'JobCustomList4', list_4, list_4, false, 29);
insert into fields (id, name, default_value, alias, enabled, "order") values (70, 'JobCustomList5', list_5, list_5, false, 30);
insert into fields (id, name, default_value, alias, enabled, "order") values (71, 'JobCustomList6', list_6, list_6, false, 31);

insert into fields (id, name, default_value, alias, enabled, "order") values (72, 'ContactCustomField1', field_1, field_1, false, 17);
insert into fields (id, name, default_value, alias, enabled, "order") values (73, 'ContactCustomField2', field_2, field_2, false, 18);
insert into fields (id, name, default_value, alias, enabled, "order") values (74, 'ContactCustomField3', field_3, field_3, false, 19);
insert into fields (id, name, default_value, alias, enabled, "order") values (75, 'ContactCustomField4', field_4, field_4, false, 20);
insert into fields (id, name, default_value, alias, enabled, "order") values (76, 'ContactCustomField5', field_5, field_5, false, 21);
insert into fields (id, name, default_value, alias, enabled, "order") values (77, 'ContactCustomField6', field_6, field_6, false, 22);
insert into fields (id, name, default_value, alias, enabled, "order") values (78, 'ContactCustomList1', list_1, list_1, false, 23);
insert into fields (id, name, default_value, alias, enabled, "order") values (79, 'ContactCustomList2', list_2, list_2, false, 24);
insert into fields (id, name, default_value, alias, enabled, "order") values (80, 'ContactCustomList3', list_3, list_3, false, 25);
insert into fields (id, name, default_value, alias, enabled, "order") values (81, 'ContactCustomList4', list_4, list_4, false, 26);
insert into fields (id, name, default_value, alias, enabled, "order") values (82, 'ContactCustomList5', list_5, list_5, false, 27);
insert into fields (id, name, default_value, alias, enabled, "order") values (83, 'ContactCustomList6', list_6, list_6, false, 28);

ALTER SEQUENCE fields_id_seq restart with 84 ;
END $$;