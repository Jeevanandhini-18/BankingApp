const API_BASE_URL = (window.BANKING_CONFIG?.apiBaseUrl || window.location.origin).replace(/\/$/, "");
const state = {
  customers: [],
  accounts: [],
  beneficiaries: [],
  selectedAccountId: null
};

const pageTitles = {
  overview: "Overview",
  customers: "Customers",
  accounts: "Accounts",
  beneficiaries: "Beneficiaries"
};

const escapeHtml = (value) => String(value ?? "").replace(/[&<>"']/g, (character) => ({
  "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
})[character]);

const amountText = (amount) => Number(amount ?? 0).toFixed(2);
const customerName = (id) => state.customers.find((customer) => customer.id === id)?.fullName ?? `Customer #${id}`;

class ApiError extends Error {
  constructor(message, status, validationErrors = {}) {
    super(message);
    this.status = status;
    this.validationErrors = validationErrors;
  }
}

async function apiRequest(path, options = {}) {
  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers: {
        ...(options.body ? { "Content-Type": "application/json" } : {}),
        ...options.headers
      }
    });
  } catch {
    throw new ApiError("Could not reach the banking API. Check that the Spring Boot app is running.", 0);
  }

  if (response.status === 204) return null;
  const contentType = response.headers.get("content-type") || "";
  const body = contentType.includes("application/json") ? await response.json() : null;
  if (!response.ok) {
    throw new ApiError(body?.message || `Request failed with status ${response.status}.`,
      response.status, body?.validationErrors || {});
  }
  return body;
}

function showNotice(message, type = "success", validationErrors = {}) {
  const notice = document.querySelector("#notice");
  notice.className = `notice${type === "error" ? " is-error" : ""}`;
  notice.replaceChildren();
  const text = document.createElement("span");
  text.textContent = message;
  notice.append(text);
  const errors = Object.entries(validationErrors);
  if (errors.length) {
    const list = document.createElement("ul");
    for (const [field, detail] of errors) {
      const item = document.createElement("li");
      item.textContent = `${field}: ${detail}`;
      list.append(item);
    }
    notice.append(list);
  }
  notice.hidden = false;
  window.clearTimeout(showNotice.timeout);
  if (type !== "error") showNotice.timeout = window.setTimeout(() => { notice.hidden = true; }, 5000);
}

function clearFieldErrors(form) {
  form.querySelectorAll("[aria-invalid='true']").forEach((input) => input.removeAttribute("aria-invalid"));
  form.querySelectorAll("[data-error-for]").forEach((message) => { message.textContent = ""; });
}

function showFieldErrors(form, errors) {
  for (const [field, message] of Object.entries(errors)) {
    const input = form.elements.namedItem(field);
    if (input && "setAttribute" in input) input.setAttribute("aria-invalid", "true");
    const output = form.querySelector(`[data-error-for="${CSS.escape(field)}"]`);
    if (output) output.textContent = message;
  }
}

function showPage(page) {
  document.querySelectorAll("[data-view]").forEach((view) => { view.hidden = view.dataset.view !== page; });
  document.querySelectorAll("[data-view-target]").forEach((button) => {
    const active = button.dataset.viewTarget === page;
    button.classList.toggle("is-active", active);
    if (button.matches(".nav-link")) {
      if (active) button.setAttribute("aria-current", "page");
      else button.removeAttribute("aria-current");
    }
  });
  document.querySelector("#page-title").textContent = pageTitles[page] || "Overview";
  document.querySelector("#notice").hidden = true;
}

function updateConnection(online) {
  document.querySelector("#connection-dot").className = `connection-dot ${online ? "is-online" : "is-offline"}`;
  document.querySelector("#connection-label").textContent = online ? "API connected" : "API unavailable";
}

async function checkConnection() {
  try {
    await apiRequest("/health");
    updateConnection(true);
  } catch {
    updateConnection(false);
  }
}

async function refreshData() {
  const [customers, accounts, beneficiaries] = await Promise.allSettled([
    apiRequest("/api/customers"),
    apiRequest("/api/accounts"),
    apiRequest("/api/beneficiaries")
  ]);
  const failures = [];
  for (const [key, result, label] of [
    ["customers", customers, "customers"],
    ["accounts", accounts, "accounts"],
    ["beneficiaries", beneficiaries, "beneficiaries"]
  ]) {
    if (result.status === "fulfilled") state[key] = result.value;
    else failures.push(`${label}: ${result.reason.message}`);
  }

  renderAll();
  await checkConnection();
  if (failures.length) showNotice(`Some data could not be loaded. ${failures.join("; ")}`, "error");
}

function renderAll() {
  renderOverview();
  renderCustomers();
  renderAccounts();
  renderBeneficiaries();
  renderCustomerSelects();
}

function renderOverview() {
  const total = state.accounts.reduce((sum, account) => sum + Number(account.balance || 0), 0);
  document.querySelector("#summary-balance").textContent = amountText(total);
  document.querySelector("#summary-customers").textContent = state.customers.length;
  document.querySelector("#summary-accounts").textContent = state.accounts.length;
  document.querySelector("#summary-beneficiaries").textContent = state.beneficiaries.length;
  const rows = state.accounts.slice(0, 6).map((account) => `
    <tr>
      <td><strong>Account #${escapeHtml(account.id)}</strong></td>
      <td>${escapeHtml(customerName(account.customerId))}</td>
      <td>${escapeHtml(account.accountNumber)}</td>
      <td class="align-right"><strong>${amountText(account.balance)}</strong></td>
      <td><button class="row-action" type="button" data-open-account="${escapeHtml(account.id)}">Details →</button></td>
    </tr>`).join("");
  document.querySelector("#overview-accounts").innerHTML = rows || '<tr><td colspan="5" class="empty-state">No accounts yet. Create a customer, then open an account.</td></tr>';
}

function renderCustomers() {
  document.querySelector("#customer-count").textContent = `${state.customers.length} ${state.customers.length === 1 ? "record" : "records"}`;
  const rows = state.customers.map((customer) => `
    <tr>
      <td><strong>${escapeHtml(customer.fullName)}</strong></td>
      <td>${escapeHtml(customer.email)}</td>
      <td>${escapeHtml(customer.phone || "—")}</td>
      <td><button class="row-action" type="button" data-customer-id="${escapeHtml(customer.id)}">View</button></td>
    </tr>`).join("");
  document.querySelector("#customers-table").innerHTML = rows || '<tr><td colspan="4" class="empty-state">No customers yet. Create the first profile here.</td></tr>';
}

function renderAccounts() {
  document.querySelector("#account-count").textContent = `${state.accounts.length} ${state.accounts.length === 1 ? "record" : "records"}`;
  const rows = state.accounts.map((account) => `
    <tr>
      <td><strong>${escapeHtml(account.accountNumber)}</strong><span class="secondary-cell">Account #${escapeHtml(account.id)}</span></td>
      <td>${escapeHtml(customerName(account.customerId))}</td>
      <td class="align-right"><strong>${amountText(account.balance)}</strong></td>
      <td><button class="row-action" type="button" data-open-account="${escapeHtml(account.id)}">Details →</button></td>
    </tr>`).join("");
  document.querySelector("#accounts-table").innerHTML = rows || '<tr><td colspan="4" class="empty-state">No accounts found.</td></tr>';
}

function renderBeneficiaries() {
  document.querySelector("#beneficiary-count").textContent = `${state.beneficiaries.length} ${state.beneficiaries.length === 1 ? "record" : "records"}`;
  const rows = state.beneficiaries.map((beneficiary) => `
    <tr>
      <td><strong>${escapeHtml(beneficiary.name)}</strong></td>
      <td>${escapeHtml(customerName(beneficiary.customerId))}</td>
      <td>${escapeHtml(beneficiary.bankName || "—")}</td>
      <td>${escapeHtml(beneficiary.accountNumber)}</td>
      <td><button class="row-action danger" type="button" data-delete-beneficiary="${escapeHtml(beneficiary.id)}">Delete</button></td>
    </tr>`).join("");
  document.querySelector("#beneficiaries-table").innerHTML = rows || '<tr><td colspan="5" class="empty-state">No beneficiaries saved yet.</td></tr>';
}

function renderCustomerSelects() {
  document.querySelectorAll("select[name='customerId']").forEach((select) => {
    const currentValue = select.value;
    select.innerHTML = '<option value="">Choose a customer</option>' + state.customers.map((customer) =>
      `<option value="${escapeHtml(customer.id)}">${escapeHtml(customer.fullName)} · #${escapeHtml(customer.id)}</option>`).join("");
    if (state.customers.some((customer) => String(customer.id) === currentValue)) select.value = currentValue;
  });
}

async function showCustomer(id) {
  try {
    const customer = await apiRequest(`/api/customers/${encodeURIComponent(id)}`);
    const detail = document.querySelector("#customer-detail");
    detail.innerHTML = `<h4>${escapeHtml(customer.fullName)}</h4><p>${escapeHtml(customer.email)}</p><p>${escapeHtml(customer.phone || "No phone on file")}</p>`;
    detail.hidden = false;
  } catch (error) {
    showNotice(error.message, "error", error.validationErrors);
  }
}

async function showAccount(id) {
  state.selectedAccountId = id;
  const detail = document.querySelector("#account-detail");
  detail.hidden = false;
  document.querySelector("#detail-account-number").textContent = "Loading account…";
  try {
    const [account, transactions] = await Promise.all([
      apiRequest(`/api/accounts/${encodeURIComponent(id)}`),
      apiRequest(`/api/accounts/${encodeURIComponent(id)}/transactions`)
    ]);
    if (String(state.selectedAccountId) !== String(id)) return;
    document.querySelector("#detail-account-number").textContent = account.accountNumber;
    document.querySelector("#detail-account-balance").textContent = amountText(account.balance);
    document.querySelector("#detail-account-owner").textContent = `${customerName(account.customerId)} · Account #${account.id}`;
    document.querySelector("#transaction-count").textContent = `${transactions.length} ${transactions.length === 1 ? "entry" : "entries"}`;
    const entries = transactions.map((transaction) => `
      <div class="transaction-row">
        <div><strong>${escapeHtml(transaction.description || transaction.type)}</strong><small>${escapeHtml(new Date(transaction.createdAt).toLocaleString())}</small></div>
        <span class="transaction-amount ${transaction.type === "DEPOSIT" ? "deposit" : "withdrawal"}">${transaction.type === "DEPOSIT" ? "+" : "−"}${amountText(transaction.amount)}</span>
      </div>`).join("");
    document.querySelector("#transaction-list").innerHTML = entries || '<p class="empty-state">No transactions recorded for this account.</p>';
  } catch (error) {
    document.querySelector("#detail-account-number").textContent = "Could not load account";
    showNotice(error.message, "error", error.validationErrors);
  }
}

async function submitForm(form, request, successMessage, afterSuccess) {
  clearFieldErrors(form);
  const submit = form.querySelector("button[type='submit']");
  const previousText = submit.textContent;
  submit.disabled = true;
  submit.textContent = "Working…";
  try {
    await request();
    form.reset();
    showNotice(successMessage);
    await refreshData();
    if (afterSuccess) await afterSuccess();
  } catch (error) {
    showFieldErrors(form, error.validationErrors || {});
    showNotice(error.message, "error", error.validationErrors);
  } finally {
    submit.disabled = false;
    submit.textContent = previousText;
  }
}

document.querySelectorAll("[data-view-target]").forEach((button) => {
  button.addEventListener("click", () => showPage(button.dataset.viewTarget));
});

document.querySelector("[data-action='refresh']").addEventListener("click", refreshData);

document.querySelector("#customer-form").addEventListener("submit", (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  const values = new FormData(form);
  const requestBody = {
    fullName: values.get("fullName").trim(),
    email: values.get("email").trim(),
    phone: values.get("phone").trim() || null
  };
  submitForm(form, () => apiRequest("/api/customers", { method: "POST", body: JSON.stringify(requestBody) }),
    "Customer created successfully.");
});

document.querySelector("#account-form").addEventListener("submit", (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  const values = new FormData(form);
  const requestBody = { customerId: Number(values.get("customerId")), initialBalance: Number(values.get("initialBalance")) };
  submitForm(form, () => apiRequest("/api/accounts", { method: "POST", body: JSON.stringify(requestBody) }),
    "Account created successfully.");
});

document.querySelector("#beneficiary-form").addEventListener("submit", (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  const values = new FormData(form);
  const requestBody = {
    customerId: Number(values.get("customerId")),
    name: values.get("name").trim(),
    accountNumber: values.get("accountNumber").trim(),
    bankName: values.get("bankName").trim() || null
  };
  submitForm(form, () => apiRequest("/api/beneficiaries", { method: "POST", body: JSON.stringify(requestBody) }),
    "Beneficiary saved successfully.");
});

document.querySelector("#transaction-form").addEventListener("submit", (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  const values = new FormData(form);
  const accountId = state.selectedAccountId;
  if (!accountId) return showNotice("Select an account before recording a transaction.", "error");
  const requestBody = {
    type: values.get("type"),
    amount: Number(values.get("amount")),
    description: values.get("description").trim() || null
  };
  submitForm(form, () => apiRequest(`/api/accounts/${encodeURIComponent(accountId)}/transactions`, {
    method: "POST", body: JSON.stringify(requestBody)
  }), "Transaction recorded successfully.", () => showAccount(accountId));
});

document.addEventListener("click", async (event) => {
  const customerButton = event.target.closest("[data-customer-id]");
  if (customerButton) return showCustomer(customerButton.dataset.customerId);
  const accountButton = event.target.closest("[data-open-account]");
  if (accountButton) {
    showPage("accounts");
    return showAccount(accountButton.dataset.openAccount);
  }
  const deleteButton = event.target.closest("[data-delete-beneficiary]");
  if (deleteButton) {
    const id = deleteButton.dataset.deleteBeneficiary;
    if (!window.confirm("Delete this beneficiary? This cannot be undone.")) return;
    deleteButton.disabled = true;
    try {
      await apiRequest(`/api/beneficiaries/${encodeURIComponent(id)}`, { method: "DELETE" });
      showNotice("Beneficiary deleted.");
      await refreshData();
    } catch (error) {
      showNotice(error.message, "error", error.validationErrors);
    } finally {
      deleteButton.disabled = false;
    }
  }
});

document.querySelector("#today-label").textContent = new Intl.DateTimeFormat(undefined, {
  weekday: "short", month: "short", day: "numeric", year: "numeric"
}).format(new Date());

refreshData();