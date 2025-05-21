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
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (16, 'JobOwner', 'Owner', 'Owner', true, 4, 'owner');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (17, 'JobLockBox', 'Lock Box Combo', 'Lock Box Combo', true, 5, 'lockBox');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (18, 'JobDirections', 'Directions to Job Site', 'Directions to Job Site', true, 6, 'directions');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (19, 'JobNotes', 'Job Site Notes', 'Job Site Notes', true, 7, 'notes');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (20, 'JobAddress', 'Address', 'Address', true, 8, 'address');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (21, 'JobPhones', 'Owner''s Phone #s', 'Owner''s Phone #s', true, 13, 'phones');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (22, 'JobEmail', 'Email', 'Email', true, 18, 'email');

insert into fields (id, name, default_value, alias, enabled, field_order, path) values (23, 'ContactCompany', 'Company', 'Company', true, 1, 'company');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (24, 'ContactProfession', 'Profession', 'Profession', true, 2, 'profession');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (25, 'ContactPerson', 'Person''s Name', 'Person''s Name', true, 3, 'person');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (26, 'ContactLastName', 'Last Name', 'Last Name', true, 4, 'lastName');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (27, 'ContactFirstName', 'First', 'First', true, 5, 'firstName');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (28, 'ContactSupervisor', 'Supervisor', 'Supervisor', true, 6, 'supervisor');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (29, 'ContactSpouse', 'Spouse', 'Spouse', true, 7, 'spouse');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (30, 'ContactTaxID', 'Tax ID', 'Tax ID', true, 8, 'taxId');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (31, 'ContactWebSite', 'Web Site', 'Web Site', true, 9, 'webSite');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (32, 'ContactWorkersCompDate', 'Workers Comp good through', 'Workers Comp good through', true, 10, 'workersCompDate');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (33, 'ContactInsuranceDate', 'Insurance good through', 'Insurance good through', true, 11, 'insuranceDate');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (34, 'ContactComments', 'Comments', 'Comments', true, 12, 'comments');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (35, 'ContactNotes', 'Notes', 'Notes', true, 13, 'notes');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (36, 'ContactAddresses', 'Addresses', 'Addresses', true, 14, 'addresses');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (37, 'ContactEmail', 'EMail Address', 'EMail Address', true, 15, 'email');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (38, 'ContactPhones', 'Contact phone #s', 'Contact phone #s', true, 16, 'phones');

insert into fields (id, name, default_value, alias, enabled, field_order, path) values (39, 'TaskAttachments', 'Files', 'Files', false, 25, 'files');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (40, 'TaskSecondaryContacts', 'Secondary Contacts', 'Secondary Contacts', false, 26, 'secondaryContacts');

insert into fields (id, name, default_value, alias, enabled, field_order, path) values (41, 'TaskCustomField1', field_1, field_1, false, 13, 'CustomField1');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (42, 'TaskCustomField2', field_2, field_2, false, 14, 'CustomField2');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (43, 'TaskCustomField3', field_3, field_3, false, 15, 'CustomField3');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (44, 'TaskCustomField4', field_4, field_4, false, 16, 'CustomField4');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (45, 'TaskCustomField5', field_5, field_5, false, 17, 'CustomField5');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (46, 'TaskCustomField6', field_6, field_6, false, 18, 'CustomField6');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (47, 'TaskCustomList1', list_1, list_1, false, 19, 'CustomList1');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (48, 'TaskCustomList2', list_2, list_2, false, 20, 'CustomList2');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (49, 'TaskCustomList3', list_3, list_3, false, 21, 'CustomList3');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (50, 'TaskCustomList4', list_4, list_4, false, 22, 'CustomList4');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (51, 'TaskCustomList5', list_5, list_5, false, 23, 'CustomList5');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (52, 'TaskCustomList6', list_6, list_6, false, 24, 'CustomList6');

insert into fields (id, name, default_value, alias, enabled, field_order, path) values (53, 'JobCustomField1', field_1, field_1, false, 20, 'CustomField1');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (54, 'JobCustomField2', field_2, field_2, false, 21, 'CustomField2');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (55, 'JobCustomField3', field_3, field_3, false, 22, 'CustomField3');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (56, 'JobCustomField4', field_4, field_4, false, 23, 'CustomField4');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (57, 'JobCustomField5', field_5, field_5, false, 24, 'CustomField5');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (58, 'JobCustomField6', field_6, field_6, false, 25, 'CustomField6');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (59, 'JobCustomList1', list_1, list_1, false, 26, 'CustomList1');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (60, 'JobCustomList2', list_2, list_2, false, 27, 'CustomList2');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (61, 'JobCustomList3', list_3, list_3, false, 28, 'CustomList3');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (62, 'JobCustomList4', list_4, list_4, false, 29, 'CustomList4');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (63, 'JobCustomList5', list_5, list_5, false, 30, 'CustomList5');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (64, 'JobCustomList6', list_6, list_6, false, 31, 'CustomList6');

insert into fields (id, name, default_value, alias, enabled, field_order, path) values (65, 'ContactCustomField1', field_1, field_1, false, 17, 'CustomField1');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (66, 'ContactCustomField2', field_2, field_2, false, 18, 'CustomField2');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (67, 'ContactCustomField3', field_3, field_3, false, 19, 'CustomField3');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (68, 'ContactCustomField4', field_4, field_4, false, 20, 'CustomField4');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (69, 'ContactCustomField5', field_5, field_5, false, 21, 'CustomField5');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (70, 'ContactCustomField6', field_6, field_6, false, 22, 'CustomField6');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (71, 'ContactCustomList1', list_1, list_1, false, 23, 'CustomList1');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (72, 'ContactCustomList2', list_2, list_2, false, 24, 'CustomList2');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (73, 'ContactCustomList3', list_3, list_3, false, 25, 'CustomList3');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (74, 'ContactCustomList4', list_4, list_4, false, 26, 'CustomList4');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (75, 'ContactCustomList5', list_5, list_5, false, 27, 'CustomList5');
insert into fields (id, name, default_value, alias, enabled, field_order, path) values (76, 'ContactCustomList6', list_6, list_6, false, 28, 'CustomList6');

ALTER SEQUENCE fields_id_seq restart with 77 ;
END $$;