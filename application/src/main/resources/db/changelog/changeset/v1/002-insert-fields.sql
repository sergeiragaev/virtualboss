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

insert into fields (id, name, default_value, alias, enabled, field_order, path) values (1, 'TaskNumber', 'Task ID', 'Task ID', true, 1, 'number');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (2, 'TaskDescription', 'Task Description', 'Task Description', true, 2, 'description');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (3, 'TaskTargetStart', 'Target Start', 'Target Start', true, 3, 'targetStart');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (4, 'TaskDuration', 'Duration', 'Duration', true, 4, 'duration');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (5, 'TaskTargetFinish', 'Target Finish', 'Target Finish', true, 5, 'targetFinish');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (6, 'TaskActualFinish', 'Actual Finish', 'Actual Finish', false, 6, 'actualFinish');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (7, 'TaskOrder', 'Task #', 'Task #', false, 7, 'taskOrder');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (8, 'TaskStatus', 'Status', 'Status', false, 8, 'status');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (9, 'TaskFollows', 'Follows Task', 'Follows Task', false, 9, 'follows');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (10, 'TaskRequested', 'Requested by', 'Requested by', false, 10, 'requested');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (11, 'TaskNotes', 'Detailed Task Notes', 'Detailed Task Notes', false, 11, 'notes');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (12, 'TaskMarked', 'Marked', 'Marked', false, 12, 'marked');

insert into fields (id, name, default_value, alias, enabled, field_order, path) values (13, 'JobNumber', 'Job #', 'Job #', true, 1, 'number');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (14, 'JobLot', 'Lot #', 'Lot #', true, 2, 'lot');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (15, 'JobSubdivision', 'Subdivision', 'Subdivision', true, 3, 'subdivision');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (16, 'JobOwnerName', 'Owner''s Name', 'Owner''s Name', true, 4, 'ownerName');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (17, 'JobLockBox', 'Lock Box Combo', 'Lock Box Combo', true, 5, 'lockBox');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (18, 'JobDirections', 'Directions to Job Site', 'Directions to Job Site', true, 6, 'directions');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (19, 'JobNotes', 'Job Site Notes', 'Job Site Notes', true, 7, 'notes');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (20, 'JobAddress1', 'Address', 'Address', true, 8, 'address1');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (21, 'JobAddress2', 'Address 2', 'Address 2', true, 9, 'address2');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (22, 'JobCity', 'City', 'City', true, 10, 'city');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (23, 'JobState', 'State', 'State', true, 11, 'state');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (24, 'JobPostal', 'Postal', 'Postal', true, 12, 'postal');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (25, 'JobHomePhone', 'Owner''s Home#', 'Owner''s Home#', true, 13, 'homePhone');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (26, 'JobWorkPhone', 'Work #', 'Work #', true, 14, 'workPhone');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (27, 'JobCellPhone', 'Cell #', 'Cell #', true, 15, 'cellPhone');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (28, 'JobFax', 'Fax number', 'Fax number', true, 16, 'fax');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (29, 'JobCompany', 'Company Name', 'Company Name', true, 17, 'company');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (30, 'JobEmail', 'Email', 'Email', true, 18, 'email');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (31, 'JobCountry', 'Country/Region', 'Country/Region', true, 19, 'country');

insert into fields (id, name, default_value, alias, enabled, field_order, path) values (32, 'ContactCompany', 'Company', 'Company', true, 1, 'company');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (33, 'ContactProfession', 'Profession', 'Profession', true, 2, 'profession');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (34, 'ContactPerson', 'Person''s Name', 'Person''s Name', true, 3, 'person');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (35, 'ContactLastName', 'Last Name', 'Last Name', true, 4, 'lastName');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (36, 'ContactFirstName', 'First', 'First', true, 5, 'firstName');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (37, 'ContactSupervisor', 'Supervisor', 'Supervisor', true, 6, 'supervisor');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (38, 'ContactSpouse', 'Spouse', 'Spouse', true, 7, 'spouse');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (39, 'ContactTaxID', 'Tax ID', 'Tax ID', true, 8, 'taxId');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (40, 'ContactWebSite', 'Web Site', 'Web Site', true, 9, 'webSite');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (41, 'ContactWorkersCompDate', 'Workers Comp good through', 'Workers Comp good through', true, 10, 'workersCompDate');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (42, 'ContactInsuranceDate', 'Insurance good through', 'Insurance good through', true, 11, 'insuranceDate');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (43, 'ContactComments', 'Comments', 'Comments', true, 12, 'comments');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (44, 'ContactNotes', 'Notes', 'Notes', true, 13, 'notes');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (45, 'ContactFax', 'Fax #', 'Fax #', true, 14, 'fax');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (46, 'ContactEmail', 'EMail Address', 'EMail Address', true, 15, 'email');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (47, 'ContactPhones', 'Contact phone #s', 'Contact phone #s', true, 16, 'phones');

insert into fields (id, name, default_value, alias, enabled, field_order, path) values (48, 'TaskCustomField1', field_1, field_1, false, 13, 'CustomField1');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (49, 'TaskCustomField2', field_2, field_2, false, 14, 'CustomField2');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (50, 'TaskCustomField3', field_3, field_3, false, 15, 'CustomField3');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (51, 'TaskCustomField4', field_4, field_4, false, 16, 'CustomField4');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (52, 'TaskCustomField5', field_5, field_5, false, 17, 'CustomField5');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (53, 'TaskCustomField6', field_6, field_6, false, 18, 'CustomField6');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (54, 'TaskCustomList1', list_1, list_1, false, 19, 'CustomList1');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (55, 'TaskCustomList2', list_2, list_2, false, 20, 'CustomList2');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (56, 'TaskCustomList3', list_3, list_3, false, 21, 'CustomList3');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (57, 'TaskCustomList4', list_4, list_4, false, 22, 'CustomList4');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (58, 'TaskCustomList5', list_5, list_5, false, 23, 'CustomList5');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (59, 'TaskCustomList6', list_6, list_6, false, 24, 'CustomList6');

insert into fields (id, name, default_value, alias, enabled, field_order, path) values (60, 'JobCustomField1', field_1, field_1, false, 20, 'CustomField1');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (61, 'JobCustomField2', field_2, field_2, false, 21, 'CustomField2');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (62, 'JobCustomField3', field_3, field_3, false, 22, 'CustomField3');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (63, 'JobCustomField4', field_4, field_4, false, 23, 'CustomField4');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (64, 'JobCustomField5', field_5, field_5, false, 24, 'CustomField5');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (65, 'JobCustomField6', field_6, field_6, false, 25, 'CustomField6');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (66, 'JobCustomList1', list_1, list_1, false, 26, 'CustomList1');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (67, 'JobCustomList2', list_2, list_2, false, 27, 'CustomList2');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (68, 'JobCustomList3', list_3, list_3, false, 28, 'CustomList3');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (69, 'JobCustomList4', list_4, list_4, false, 29, 'CustomList4');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (70, 'JobCustomList5', list_5, list_5, false, 30, 'CustomList5');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (71, 'JobCustomList6', list_6, list_6, false, 31, 'CustomList6');

insert into fields (id, name, default_value, alias, enabled, field_order, path) values (72, 'ContactCustomField1', field_1, field_1, false, 17, 'CustomField1');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (73, 'ContactCustomField2', field_2, field_2, false, 18, 'CustomField2');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (74, 'ContactCustomField3', field_3, field_3, false, 19, 'CustomField3');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (75, 'ContactCustomField4', field_4, field_4, false, 20, 'CustomField4');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (76, 'ContactCustomField5', field_5, field_5, false, 21, 'CustomField5');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (77, 'ContactCustomField6', field_6, field_6, false, 22, 'CustomField6');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (78, 'ContactCustomList1', list_1, list_1, false, 23, 'CustomList1');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (79, 'ContactCustomList2', list_2, list_2, false, 24, 'CustomList2');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (80, 'ContactCustomList3', list_3, list_3, false, 25, 'CustomList3');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (81, 'ContactCustomList4', list_4, list_4, false, 26, 'CustomList4');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (82, 'ContactCustomList5', list_5, list_5, false, 27, 'CustomList5');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (83, 'ContactCustomList6', list_6, list_6, false, 28, 'CustomList6');

insert into fields (id, name, default_value, alias, enabled, field_order, path) values (84, 'TaskAttachments', 'Files', 'Files', false, 25, 'files');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (85, 'TaskSecondaryContacts', 'Secondary Contacts', 'Secondary Contacts', false, 26, 'secondaryContacts');

ALTER SEQUENCE fields_id_seq restart with 86 ;
END $$;