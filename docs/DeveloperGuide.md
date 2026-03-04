## Product name: Mycelia

## Target user profile

**Target user group:** Operations planners / dispatchers / coordinators in **B2B delivery companies**.

**User characteristics:**
- Works with many clients/partners and delivery tasks daily (high-volume operations).
- Comfortable with typing and prefers keyboard-driven workflows.
- Uses a laptop/desktop during planning and tracking (office/warehouse setting).
- Needs fast retrieval of contact details and clear visibility of delivery progress.

## Value proposition

Mycelia provides a fast, keyboard-driven way to:
- store and retrieve **client and partner details** (contacts, addresses, notes), and
- track **delivery resources and delivery progress** in near real-time via status updates (e.g., pending/completed/incomplete), tags, and cut-off times.

It reduces time spent searching across spreadsheets/chats and helps teams keep delivery work organized and up-to-date.

---

## User stories (with priorities)

### P1 (Must-have)
1. **As a user**, I want to **add addresses**, so that I can store new delivery locations.
2. **As a user**, I want to **remove addresses**, so that I can keep the address book clean.
3. **As a user**, I want to **edit addresses**, so that I can correct outdated location details.
4. **As a user**, I want to **create a delivery list**, so that I can keep track of deliveries.
5. **As a user**, I want to **view delivery lists and addresses**, so that I know what to do next.
6. **As a user**, I want to **mark a delivery as complete**, so that I can track what is left.
7. **As a user**, I want to **mark a delivery as incomplete**, so that I can undo mistakes.
8. **As a user**, I want to **add a client contact** with key fields, so that I can retrieve client details quickly.
9. **As a user**, I want to **create a delivery record linked to a client contact**, so that I can track work by customer.
10. **As a forgetful user**, I want to **track all deliveries for the day**, so that I can complete them on time.

### P2 (Should-have)
11. **As a user**, I want to **tag addresses**, so that I can better sort and manage locations.
12. **As a user**, I want to **view deliveries due at each location**, so that I can track progress per stop.
13. **As a user**, I want to **tag contacts** (e.g., VIP/fragile/COD/restricted), so that I can filter for special handling.
14. **As a user**, I want to **add cut-off timings** to deliveries, so that I know which deliveries must be done first.
15. **As a user**, I want to **sort deliveries by tags/time/distance**, so that I can prioritize efficiently.

### P3 (Could-have)
16. **As a first-time user**, I want to **view a guided tour**, so that I can learn the app quickly.
17. **As a user**, I want to **add addresses using postal code/coordinates**, so that I can reduce manual typing.

### Won’t-have (for now)
18. **As a user**, I want **cloud sync across multiple devices/users**, so that multiple staff can update the same data in real time (won’t have for now due to scope and constraints).

---

## Use cases

### UC01 Create a delivery record linked to a client contact

**Actor:** User  
**Preconditions:**  
- The user has launched Mycelia.
- A client contact already exists in the system.

**Main success scenario:**
1. User searches for a client contact using a keyword (e.g., client name).
2. System displays matching client contacts.
3. User selects the intended client contact.
4. User requests to create a delivery record and provides required details (e.g., delivery address, date/time, tags, cut-off time).
5. System validates the input.
6. System creates the delivery record linked to the selected client contact.
7. System shows a confirmation message.

**Extensions:**
- **2a. No matches found:** System shows “No matching contacts found” and ends the use case.
- **5a. Invalid/missing fields:** System shows validation errors and prompts user to correct the inputs.
- **6a. Duplicate record detected:** System warns the user and asks whether to proceed or cancel.

---

### UC02 Mark a delivery as complete

**Actor:** User (dispatcher / delivery coordinator)  
**Preconditions:**  
- A delivery list for the day exists.
- At least one delivery record is currently not completed.

**Main success scenario:**
1. User requests to view today’s delivery list.
2. System displays the delivery list with current statuses.
3. User selects the target delivery record.
4. User marks the selected delivery record as **complete**.
5. System updates the delivery status.
6. System refreshes the delivery list and shows confirmation.

**Extensions:**
- **3a. Delivery not found:** System informs the user and ends the use case.
- **4a. Delivery already completed:** System warns the user and leaves the status unchanged.
- **4b. Wrong delivery chosen:** User marks the delivery as incomplete (undo) and repeats steps 3–6.

---

### UC03 Tag a client contact for special handling

**Actor:** User  
**Preconditions:**  
- The client contact exists.

**Main success scenario:**
1. User searches for a client contact.
2. System displays matching contacts.
3. User selects the target contact.
4. User adds one or more tags (e.g., VIP, fragile, COD, restricted).
5. System updates the contact record and shows confirmation.

**Extensions:**
- **2a. No matches found:** System shows “No matching contacts found” and ends the use case.
- **4a. Tag already exists:** System ignores the duplicate tag and confirms completion.

---

## Non-functional requirements (NFRs)

1. **Command-first usability:** All core features (add/edit/delete/view/search/tag/mark status) shall be usable via typed commands without requiring mouse-only operations.
2. **Performance (search):** With up to **10,000 contacts** and **1,000 delivery records**, search results shall be displayed within **2 seconds** on a typical laptop.
3. **Performance (list rendering):** With up to **1,000 delivery records**, rendering a delivery list shall complete within **2 seconds**.
4. **Reliability (data loss):** In the event of a crash or connectivity loss, the system shall not lose more than **1 minute** of user edits (autosave or frequent persistence).
5. **Portability:** The app shall run on **Windows, macOS, and Linux** using **Java 17**.
6. **Local storage:** User data shall be stored locally in a **human-editable text file**.
7. **No external server dependency:** Core features shall not depend on a custom remote server (the app remains usable without any self-hosted backend).
8. **Security (local data):** The system shall not transmit user data externally unless explicitly triggered by the user (e.g., export/share).

---

## Glossary

- **Client contact:** A customer entry (company/person) with key fields such as name, phone, address, and notes.
- **Partner:** A collaborating entity (e.g., supplier or 3PL partner) whose details are stored for coordination.
- **Delivery record:** A single delivery task, optionally linked to a client contact, containing address, timing, and status.
- **Delivery list:** A collection of delivery records grouped for a day or route.
- **Cut-off time:** The latest time by which a delivery should be completed.
- **Tag:** A label applied to contacts/addresses/deliveries for filtering and prioritization.
- **Special-handling tags:** Tags such as VIP/fragile/COD/restricted indicating extra constraints.
- **COD (Cash on Delivery):** A delivery that requires payment collection upon delivery.