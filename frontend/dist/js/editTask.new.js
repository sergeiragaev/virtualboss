// Убрали глобальную переменную
let GlobalNames = [];

// Вынесли общую обработку ошибок
const handleError = (error, defaultMessage = "An error occurred") => {
  if (error.responseText === 'InvalidLogin') {
    logout();
    return true;
  }
  console.error(defaultMessage, error);
  return false;
};

// Рефактор функции editTask с использованием промисов
async function editTask(taskID, view) {
  try {
    const taskData = await fetchData(`/api/v1/task/${taskID}`);
    const fieldNames = await fetchData(
      `/api/v1/fieldcaptions?fields=TaskTargetFinish,${[...allTaskFieldCaptionNames, ...allContactFieldCaptionNames, ...allJobFieldCaptionNames].join(",")}`
    );

    GlobalNames = fieldNames;
    showEditScreen(taskData, fieldNames, view);
  } catch (error) {
    showErrorDialog("Error Opening Task", error);
  }
}

// Универсальная функция для запросов
async function fetchData(url) {
  const response = await $.ajax({
    url,
    dataType: 'json',
    error: (jqXHR, textStatus, errorThrown) => {
      if (!handleError(jqXHR)) {
        throw new Error(errorThrown);
      }
    }
  });

  if (response === 'InvalidLogin') {
    logout();
    throw new Error('Invalid login');
  }

  return response;
}

async function showEditScreen(data, names, view) {
  await buildEditForm(data, names, view);
}

// Компонент для создания полей дат
function createDateFields(data, names) {
  return `
        <div class="row form-group">
            ${createDateInput('TargetStart', names.TaskTargetStart, data.TaskTargetStart)}
            ${createDurationInput(data.TaskDuration, names)}
            ${createDateInput('TargetFinish', names.TaskTargetFinish, data.TaskTargetFinish, true)}
        </div>
    `;
}

// Универсальный компонент для input
function createInput(fieldName, label, value, options = {}) {
  return `
        <div class="${options.groupClass || 'form-group'}">
            ${label ? `<label>${label}</label>` : ''}
            <input type="${options.type || 'text'}" 
                   class="form-control ${options.classes || ''}" 
                   name="${fieldName}" 
                   value="${escapeHtml(value)}"
                   ${options.attrs || ''}>
        </div>
    `;
}

// Экранирование HTML
function escapeHtml(str) {
  if (str === undefined) return;
  return str.replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

// Рефактор обработки кастомных полей
function createCustomFields(data, names) {
  return `
        <div class="row">
            <div class="col-xs-12 col-sm-6">
                ${[1,2,3,4,5,6].map(i =>
    createCustomField(i, data, names, 'TaskCustomField')).join('')}
            </div>
            <div class="col-xs-12 col-lg-6">
                ${[1,2,3,4,5,6].map(i =>
    createCustomList(i, data, names, 'TaskCustomList')).join('')}
            </div>
        </div>
    `;
}

// Генерация кастомного поля
function createCustomField(index, data, names, prefix) {
  const cookieName = `Show${prefix}${index}`;
  const isVisible = Cookies.get(cookieName) === 'true';

  return `
        <div class="input-group form-group ${!isVisible ? 'hidden' : ''}" 
             id="${prefix}${index}Wrap">
            <div class="input-group-btn">
                <button class="btn btn-default">${names[`${prefix}${index}`]}</button>
            </div>
            <input type="text" 
                   name="CustomField${index}" 
                   value="${escapeHtml(data[`${prefix}${index}`])}"
                   class="form-control"
                   ${!isVisible ? 'disabled' : ''}>
        </div>
    `;
}

function createCustomList(index, data, names, prefix) {
  const cookieName = `Show${prefix}${index}`;
  const isVisible = Cookies.get(cookieName) === 'true';

  return `
        <div class="input-group form-group ${!isVisible ? 'hidden' : ''}" 
             id="${prefix}${index}Wrap">
            <div class="input-group-btn">
                <button class="btn btn-default">${names[`${prefix}${index}`]}</button>
            </div>
            <input type="text" 
                   name="CustomField${index}" 
                   value="${escapeHtml(data[`${prefix}${index}`])}"
                   class="form-control"
                   ${!isVisible ? 'disabled' : ''}>
        </div>
    `;
}

// Рефактор диалога редактирования
function renderEditDialog(data, body, view) {
  const dialog = new BootstrapDialog({
    title: escapeHtml(data.TaskDescription),
    message: body,
    size: BootstrapDialog.SIZE_WIDE,
    buttons: createDialogButtons(data, view)
  });

  dialog.onShown(() => initializeDatePickers(data));
  dialog.open();
}

// Создает скрытое поле ввода
function createHiddenInput(name, value) {
  return `<input type="hidden" name="${name}" id="${name}" value="${escapeHtml(value)}">`;
}

// Создает группу формы с меткой и полем ввода
function createFormGroup(fieldName, label, value, options = {}) {
  return `
        <div class="form-group">
            <label>${label}</label>
            <input type="${options.type || 'text'}" 
                   class="form-control ${options.extraClasses || ''}" 
                   name="${fieldName}" 
                   value="${escapeHtml(value)}"
                   ${options.attributes || ''}>
        </div>
    `;
}

// Создает секцию Job/Contact с выпадающими меню
function createJobContactSection(data, names) {
  return `
        <div class="row">
            ${createJobSection(data, names)}
            ${createContactSection(data, names)}
        </div>
    `;
}

// Создает поле ввода длительности с обработчиком изменения
function createDurationInput(duration, names) {
  return `
        <div class='col-xs-2' style='padding-left:5px; padding-right:5px;'>
            <label>${names.TaskDuration}</label>
            <input type="number" 
                   class="form-control" 
                   name="Duration" 
                   min="1" 
                   value="${duration}"
                   onchange="updateTargetFinish()">
        </div>
    `;
}

// Создает секцию Order и Status
function createOrderStatusSection(data, names) {
  return `
        <div class="row form-group">
            <div class="col-lg-4 col-xs-6">
                ${createFormGroup('Order', names.TaskOrder, data.TaskOrder)}
            </div>
            <div class="col-lg-4 col-xs-6">
                ${createStatusDropdown(data.TaskStatus, names)}
            </div>
            <div class='col-xs-12 col-lg-4'>
                ${createRequestedBySection(data, names)} // Асинхронный вызов
            </div>
        </div>
    `;
}

// Создает текстовое поле для заметок
function createNotesField(label, value) {
  return `
        <div class="form-group">
            <label>${label}</label>
            <textarea class="form-control" 
                      name="Notes" 
                      style="min-height:150px; height:auto; white-space:pre-wrap;">
                ${escapeHtml(value)}
            </textarea>
        </div>
    `;
}

// Создает панель инструментов с кнопками
function createToolbar(data) {
  return `
        <div class="btn-group">
            <button type="button" 
                    class="btn btn-default"
                    onclick="editTaskGroups('${data.TaskId}','${data.TaskGroups}')"
                    title="Add to groups">
                <i class="fa fa-object-group"></i>
                <span class="hidden-xs"> Groups</span>
            </button>
            ${createAttachmentDropdown(data.TaskFiles)}
        </div>
    `;
}

// Создает кнопки для диалогового окна
function createDialogButtons(data, view) {
  return [
    {
      label: '<i class="fa fa-trash-o text-danger"></i> <span class="hidden-xs text-danger">Delete</span>',
      cssClass: 'btn-default',
      action: dialog => confirmDeleteTask(data, dialog, view)
    },
    {
      label: 'Cancel',
      cssClass: 'btn-default',
      action: dialog => dialog.close()
    },
    {
      label: '<i class="fa fa-save"></i> Save',
      cssClass: 'btn-primary',
      action: dialog => saveTaskChanges(data.TaskId, dialog, view)
    }
  ];
}

// Инициализирует datepicker для полей дат
function initializeDatePickers(data) {
  const dateFields = [
    { selector: 'input[name="TargetStart"]', options: { daysOfWeekDisabled: [0,6] } },
    { selector: 'input[name="ActualFinish"]', options: { daysOfWeekDisabled: [0,6] } }
  ];

  dateFields.forEach(({ selector, options }) => {
    $(selector).datepicker({
      format: 'mm/dd/yyyy',
      autoclose: true,
      todayHighlight: true,
      orientation: 'auto',
      ...options
    });
  });

  if (data.TaskFollows) {
    setupDependentDateLogic(data);
  }
}

// Вспомогательные функции
function createStatusDropdown(currentStatus, names) {
  return `
        <label>${names.TaskStatus}</label>
        <div class="input-group">
            <div class="input-group-btn">
                <button type="button" 
                        class="btn btn-default dropdown-toggle" 
                        data-toggle="dropdown">
                    ${names.TaskStatus} <span class="caret"></span>
                </button>
                <ul class="dropdown-menu">
                    <li><a href="#" onclick="setTaskStatus('Active')">Active</a></li>
                    <li><a href="#" onclick="setTaskStatus('Done')">Done</a></li>
                </ul>
            </div>
            <input type="text" 
                   class="form-control" 
                   name="Status" 
                   value="${currentStatus}" 
                   readonly>
        </div>
    `;
}

function createAttachmentDropdown(files) {
  const items = files.split('<BR>').filter(Boolean).map(file =>
    `<li>${escapeHtml(file)}</li>`
  ).join('');

  return `
        <div class="btn-group dropdown">
            <button class="btn btn-default dropdown-toggle" 
                    type="button" 
                    data-toggle="dropdown">
                <i class="fa fa-paperclip"></i>
                <span class="hidden-xs"> Attachments</span>
                <span class="caret"></span>
            </button>
            <ul class="dropdown-menu">
                ${items.length > 0 ? items : '<li><a>No attachments</a></li>'}
            </ul>
        </div>
    `;
}

// Создает секцию выбора Job
function createJobSection(data, names) {
  return `
        <div class="col-xs-12 col-sm-6">
            <div class="input-group form-group">
                <div class="input-group-btn">
                    ${createDropdownButton(names.JobNumber, 'chooseFromJobList()')}
                    ${createDropdownMenu([
    {text: `Choose From ${names.JobNumber} List`, action: 'chooseFromJobList()'},
    {divider: true},
    {text: 'Remove', action: 'unassignJob()'}
  ])}
                </div>
                <input readonly type="text" 
                       name="JobNumber" 
                       id="JobNumber" 
                       class="form-control" 
                       value="${escapeHtml(data.JobNumber)}">
                ${createHiddenInput('JobId', data.JobId)}
            </div>
        </div>
    `;
}

// Создает секцию выбора Contact
function createContactSection(data, names) {
  return `
        <div class="col-xs-12 col-sm-6">
            <div class="input-group form-group">
                <div class="input-group-btn">
                    ${createDropdownButton(names.ContactPerson, 'chooseFromContactList()')}
                    ${createDropdownMenu([
    {text: 'Choose From Contacts', action: 'chooseFromContactList()'},
    {divider: true},
    {text: 'Remove', action: 'unassignContact()'}
  ])}
                </div>
                <input readonly id="ContactName" 
                       type="text" 
                       class="form-control" 
                       value="${escapeHtml(data.ContactPerson)}">
                ${createHiddenInput('ContactId', data.ContactId)}
            </div>
        </div>
    `;
}

// Создает поле ввода даты с календарем
function createDateInput(fieldName, label, value, isReadonly = false) {
  return `
        <div class="col-xs-5" style="padding-right:5px;">
            <label>${label}</label>
            <div class="input-group date">
                <input name="${fieldName}" 
                       class="form-control" 
                       value="${moment(value).format('MM/DD/YYYY')}"
                       style="background-color:#fff;" 
                       ${isReadonly ? 'readonly' : ''}>
                <div class="input-group-addon">
                    <i class="fa fa-calendar"></i>
                </div>
            </div>
        </div>
    `;
}

// Подтверждение удаления задачи
function confirmDeleteTask(data, dialog, view) {
  const confirmDialog = new BootstrapDialog({
    type: BootstrapDialog.TYPE_DANGER,
    title: "Confirm Delete",
    message: `Are you sure you want to delete <strong>${escapeHtml(data.TaskDescription)}</strong>?`,
    buttons: [{
      label: "Delete",
      cssClass: "btn-danger",
      action: () => {
        deleteTask(data.TaskId, view);
        dialog.close();
        confirmDialog.close();
      }
    }, {
      label: "Cancel",
      action: () => confirmDialog.close()
    }]
  });
  confirmDialog.open();
}

// Сохранение изменений задачи
async function saveTaskChanges(taskId, dialog, view) {
  try {
    const formData = $("form[name='details']").serialize();
    await $.ajax({
      url: `/api/v1/task/${taskId}`,
      method: 'PUT',
      data: formData
    });
    showSuccessNotification('Task saved!');
    refreshView(view);
    dialog.close();
  } catch (error) {
    handleSaveError(error);
  }
}

// Настройка логики зависимых дат
function setupDependentDateLogic(data) {
  const $targetStart = $("input[name='TargetStart']");
  const $finishPlus = $("input[name='FinishPlus']");

  const updateStartDate = () => {
    const finishPlusDays = parseInt($finishPlus.val()) || 0;
    const newStart = moment(data.TaskTargetFinish)
      .businessAdd(finishPlusDays)
      .format('MM/DD/YYYY');
    $targetStart.val(newStart);
    updateTargetFinish();
  };

  $finishPlus.on('change', updateStartDate);
  updateStartDate();
}

// Установка статуса задачи
function setTaskStatus(status) {
  const $statusInput = $("input[name='Status']");
  const $actualFinishSection = $("#actualFinishSection");

  $statusInput.val(status);

  if (status === 'Done') {
    const finishDate = $("input[name='TargetFinish']").val();
    $("input[name='ActualFinish']").val(finishDate);
    $actualFinishSection.removeClass('hidden');
  } else {
    $("input[name='ActualFinish']").val('');
    $actualFinishSection.addClass('hidden');
  }

  return false; // Prevent default link behavior
}

// Вспомогательные функции
function createDropdownButton(text, onClick) {
  return `
        <button class="btn btn-default" 
                type="button" 
                onclick="${onClick}">
            ${text}
        </button>
        <button type="button" 
                class="btn btn-default dropdown-toggle" 
                data-toggle="dropdown">
            <span class="caret"></span>
        </button>
    `;
}

function createDropdownMenu(items) {
  return `
        <ul class="dropdown-menu">
            ${items.map(item =>
    item.divider ? '<li role="separator" class="divider"></li>' :
      `<li><a href="#" onclick="${item.action}">${item.text}</a></li>`
  ).join('')}
        </ul>
    `;
}

async function deleteTask(taskId, view) {
  try {
    await $.ajax({
      url: `/api/v1/task/${taskId}`,
      method: 'DELETE'
    });
    refreshView(view);
  } catch (error) {
    showErrorDialog('Delete Error', error);
  }
}

function refreshView(view) {
  switch(view) {
    case 'Calendar': loadCalendarView(); break;
    case 'TaskManager': createTaskList(); break;
    case 'GanttChart': loadGanttChart(); break;
  }
}

function handleSaveError(error) {
  if (error.responseJSON?.message) {
    showErrorDialog('Save Error', error.responseJSON.message);
  } else {
    showErrorDialog('Save Error', error.statusText);
  }
}

function unassignJob(){
  $("#JobNumber").val("");
}

function unassignContact(){
  $("#ContactName").val("");
  $("input[name=ContactId]").val("UNASSIGNED");
}

function showErrorDialog(title, message, options = {}) {
  // Параметры по умолчанию
  const config = {
    dialogType: BootstrapDialog.TYPE_DANGER,
    buttonLabel: 'OK',
    buttonClass: 'btn-default',
    size: BootstrapDialog.SIZE_NORMAL,
    escapeHtml: true,
    ...options
  };

  // Экранирование HTML если требуется
  const safeTitle = config.escapeHtml ? escapeHtml(title) : title;
  const safeMessage = config.escapeHtml ? escapeHtml(message) : message;

  // Создаем HTML структуру
  const dialogContent = `
        <div class="alert alert-${config.dialogType.toLowerCase().replace('type_', '')}">
            <div class="error-message-content">${safeMessage}</div>
        </div>
    `;

  // Инициализация диалога
  const dialog = new BootstrapDialog({
    title: `<i class="fa fa-exclamation-circle"></i> ${safeTitle}`,
    message: dialogContent,
    type: config.dialogType,
    size: config.size,
    buttons: [{
      label: config.buttonLabel,
      cssClass: config.buttonClass,
      action: dialog => dialog.close()
    }],
    onshown: () => {
      // Автофокус на кнопку
      dialog.getButton('btn-0').focus();
    }
  });

  dialog.open();
}

function updateTargetFinish(){
  var duration = $("input[name=TaskDuration]").val();
  duration--;

  $("input[name=TargetFinish]").val(moment($("input[name=TargetStart]").val(), "MM/DD/YYYY").add(duration, "days").format("MM/DD/YYYY"));
}

// Выбор из списка контактов
async function chooseFromContactList() {
  try {
    const contacts = await fetchData('/api/v1/contact?fields=ContactId,ContactPerson,ContactCompany');
    renderSelectionDialog({
      title: 'Select Contact',
      searchPlaceholder: 'Search contacts...',
      data: contacts.content,
      displayField: 'ContactPerson',
      subField: 'ContactCompany',
      onSelect: (selected) => {
        $('#ContactName').val(`${selected.ContactPerson} (${selected.ContactCompany})`);
        $('input[name="ContactId"]').val(selected.ContactId);
      }
    });
  } catch (error) {
    showErrorDialog('Contact Selection Error', error);
  }
}

// Выбор из списка работ
async function chooseFromJobList() {
  try {
    const jobs = await fetchData('/api/v1/job?fields=JobId,JobNumber');
    renderSelectionDialog({
      title: 'Select Job',
      searchPlaceholder: 'Search jobs...',
      data: jobs.content,
      displayField: 'JobNumber',
      onSelect: (selected) => {
        $('#JobNumber').val(selected.JobNumber);
        $('input[name="JobId"]').val(selected.JobId);
      }
    });
  } catch (error) {
    showErrorDialog('Job Selection Error', error);
  }
}

// Общая функция для отображения диалога выбора
function renderSelectionDialog(config) {
  const dialogContent = `
        <div class="selection-dialog">
            <div class="search-box">
                <input type="text" 
                       class="form-control" 
                       placeholder="${config.searchPlaceholder}"
                       id="selectionSearch">
            </div>
            <div class="items-list" style="max-height: 400px; overflow-y: auto;">
                ${config.data.map(item => `
                    <div class="selection-item" 
                         data-id="${item[Object.keys(item)[0]]}"
                         onclick="handleItemSelection(this, ${JSON.stringify(item)})">
                        <div class="main-text">${escapeHtml(item[config.displayField])}</div>
                        ${config.subField ? `<div class="sub-text">${escapeHtml(item[config.subField])}</div>` : ''}
                    </div>
                `).join('')}
            </div>
        </div>
    `;

  const dialog = new BootstrapDialog({
    title: config.title,
    message: dialogContent,
    size: BootstrapDialog.SIZE_WIDE,
    buttons: [{
      label: 'Close',
      action: dialog => dialog.close()
    }]
  });

  // Поиск в реальном времени
  dialog.onShown(() => {
    $('#selectionSearch').on('input', function() {
      const searchTerm = $(this).val().toLowerCase();
      $('.selection-item').each(function() {
        const text = $(this).text().toLowerCase();
        $(this).toggle(text.includes(searchTerm));
      });
    });
  });

  dialog.open();

  // Обработчик выбора
  window.handleItemSelection = (element, item) => {
    $(element).addClass('selected').siblings().removeClass('selected');
    config.onSelect(item);
  };
}

// Редактирование групп задач
async function editTaskGroups(taskId, currentGroups) {
  try {
    const allGroups = await fetchData('/api/v1/taskGroup');
    const currentGroupIds = currentGroups.split(',').filter(Boolean);

    const dialogContent = `
            <form class="task-group-editor">
                <div class="group-list">
                    ${allGroups.map(group => `
                        <label class="group-item">
                            <input type="checkbox" 
                                   name="group" 
                                   value="${group.Id}"
                                   ${currentGroupIds.includes(group.Id) ? 'checked' : ''}>
                            ${escapeHtml(group.Name)}
                        </label>
                    `).join('')}
                </div>
                <button type="button" 
                        class="btn btn-primary btn-block"
                        onclick="saveTaskGroups('${taskId}')">
                    Save Groups
                </button>
            </form>
        `;

    const dialog = new BootstrapDialog({
      title: 'Edit Task Groups',
      message: dialogContent,
      size: BootstrapDialog.SIZE_NORMAL
    });

    dialog.open();
  } catch (error) {
    showErrorDialog('Group Editing Error', error);
  }
}

// Сохранение выбранных групп
async function saveTaskGroups(taskId) {
  try {
    const selectedGroups = Array.from(
      document.querySelectorAll('input[name="group"]:checked')
    ).map(input => input.value);

    await fetchData(`/api/v1/task/${taskId}/groups`, {
      method: 'PUT',
      body: JSON.stringify({ groups: selectedGroups })
    });

    // Обновление интерфейса
    $('input[name="Groups"]').val(selectedGroups.join(','));
    BootstrapDialog.closeAll();
    showSuccessNotification('Groups updated successfully');
  } catch (error) {
    showErrorDialog('Save Groups Error', error);
  }
}

function showSuccessNotification(message, options = {}) {
  const config = {
    duration: 3000, // Время показа в миллисекундах
    position: 'top-right', // Варианты: top-right, top-left, bottom-right, bottom-left
    showIcon: true,
    showClose: true,
    escapeHtml: true,
    ...options
  };

  // Создаем контейнер если его нет
  const containerId = `notification-container-${config.position}`;
  let container = document.getElementById(containerId);

  if (!container) {
    container = document.createElement('div');
    container.id = containerId;
    container.className = `notification-container ${config.position}`;
    document.body.appendChild(container);
  }

  // Создаем элемент уведомления
  const notification = document.createElement('div');
  notification.className = 'success-notification';

  // Экранирование HTML
  const safeMessage = config.escapeHtml ? escapeHtml(message) : message;

  // Иконка успеха
  const icon = config.showIcon ?
    '<div class="notification-icon"><i class="fa fa-check-circle"></i></div>' : '';

  // Кнопка закрытия
  const closeButton = config.showClose ?
    `<div class="notification-close" onclick="this.parentElement.remove()">&times;</div>` : '';

  notification.innerHTML = `
        ${icon}
        <div class="notification-content">${safeMessage}</div>
        ${closeButton}
    `;

  // Добавляем в DOM
  container.appendChild(notification);

  // Анимация появления
  setTimeout(() => notification.classList.add('show'), 10);

  // Автоматическое закрытие
  if (config.duration > 0) {
    setTimeout(() => {
      notification.classList.remove('show');
      setTimeout(() => notification.remove(), 300);
    }, config.duration);
  }
}

// Стили для CSS
const notificationStyles = `
    .notification-container {
        position: fixed;
        z-index: 9999;
        max-width: 350px;
        width: 100%;
        padding: 15px;
    }

    .notification-container.top-right {
        top: 20px;
        right: 20px;
    }

    .notification-container.top-left {
        top: 20px;
        left: 20px;
    }

    .notification-container.bottom-right {
        bottom: 20px;
        right: 20px;
    }

    .notification-container.bottom-left {
        bottom: 20px;
        left: 20px;
    }

    .success-notification {
        background: #d4edda;
        color: #155724;
        padding: 15px 20px;
        border-radius: 4px;
        margin-bottom: 15px;
        display: flex;
        align-items: center;
        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        transform: translateY(-20px);
        opacity: 0;
        transition: all 0.3s ease;
    }

    .success-notification.show {
        transform: translateY(0);
        opacity: 1;
    }

    .notification-icon {
        margin-right: 12px;
        font-size: 1.2em;
    }

    .notification-content {
        flex-grow: 1;
    }

    .notification-close {
        margin-left: 15px;
        cursor: pointer;
        opacity: 0.7;
        transition: opacity 0.2s;
    }

    .notification-close:hover {
        opacity: 1;
    }
`;

// Добавляем стили в документ
const styleSheet = document.createElement('style');
styleSheet.textContent = notificationStyles;
document.head.appendChild(styleSheet);


async function fetchEmployees() {
  const response = await fetch('/api/v1/employee?Id=_');
  if (!response.ok) throw new Error('Employee fetch failed');
  const data = await response.json();
  if (data === 'InvalidLogin') throw new Error('Session expired');
  return data;
}

async function createRequestedBySection(data, names) {
  try {
    const employees = await fetchEmployees();
    return `
            <div class='form form-group'>
                <br class='hidden-lg' />
                <label>${escapeHtml(names.TaskRequested)}</label>
                <select name="Requested" class="form-control">
                    <option value=""></option>
                    ${employees.map(emp => `
                        <option value="${escapeHtml(emp.Name.trim())}" 
                            ${emp.Name.trim() === data.TaskRequested ? 'selected' : ''}>
                            ${escapeHtml(emp.Name.trim())}
                        </option>
                    `).join('')}
                </select>
            </div>
        `;
  } catch (error) {
    console.error("Employee load failed:", error);
    return '<div class="alert alert-danger">Failed to load employee list</div>';
  }
}

// Интеграция в родительский компонент
async function buildEditForm(data, names, view) {
  try {
    const sections = await Promise.all([
      createHiddenInput('Id', data.TaskId),
      createFormGroup('Description', names.TaskDescription, data.TaskDescription),
      createJobContactSection(data, names),
      createDateFields(data, names),
      createOrderStatusSection(data, names),
      createCustomFields(data, names),
      createNotesField(names.TaskNotes, data.TaskNotes),
      createToolbar(data)
    ]);

    const body = `
            <form role="form" name="details">
                ${sections.join('')}
            </form>
        `;

    renderEditDialog(data, body, view);
  } catch (error) {
    showErrorDialog('Form Error', error.message);
  }
}