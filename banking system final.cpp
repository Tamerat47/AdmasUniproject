#include <iostream>
#include <fstream>
#include <ctime>
#include <iomanip>
#include <cstdlib>
#include <cstring>
#include <cctype>
#include <limits>
#include <conio.h>
#include <thread>
#include <mutex>
#include <string>
/*
This version of this program is upgraded to hide password on the interface 
and allow two way transactions can be made but the transaction side 
there is a need for central database management system or central core. This because 
transaction history needs to be written simultaneously/parallelly on one file that holds everything */

using namespace std;
mutex transferMutex;
void displayProjectInfo() {
    cout << "***************************************************\n";
    cout << "*             16-2DECS1 CLASS PROJECT             *\n";
    cout << "*             \"TAM BANKING SYSTEM\"                *\n";
    cout << "***************************************************\n";
    cout << "*                  TEAM MEMBERS                   *\n";
    cout << "*-------------------------------------------------*\n";
    cout << "*          Name              |      ID            *\n";
    cout << "*-------------------------------------------------*\n";
    cout << "*  1. Tamerat Mekonnen       |   0191/23          *\n";
    cout << "***************************************************\n\n";
}

struct Transaction {
    char date[20];
    char time[10];
    char type[10];
    double amount;
    char sender[20];
    char receiver[20];
};

struct User {
    int accountNumber;
    char username[50] = {}; // intialize to empty string
    char password[20] = {}; // intialize to
    char phone[15] = {}; // intialize to empty string
    double balance = 0.0;
    Transaction transactions[30] = {}; // intialize to empty string;
    int transactionCount = 0; // intialize to
};

// Dynamically allocated user array
User* users = nullptr;
int userCount = 0;
int userCapacity = 2;
int nextAccountNumber = 1000;

// clear the console screen
void clearScreen() {
    system("cls");  
}

//Gets the current date and time as strings
void getCurrentDateTime(char* date, char* timeStr) {
    time_t currentTime;
    time(&currentTime);
    tm* localTime = localtime(&currentTime);
    strftime(date, 20, "%Y-%m-%d", localTime);
    strftime(timeStr, 10, "%H:%M:%S", localTime);
}

//Expands the user array when full
void resizeUsersArray() {
    userCapacity *= 2;
    User* temp = new User[userCapacity];
    for (int i = 0; i < userCount; i++) {
        temp[i] = users[i];
    }
    delete[] users;
    users = temp;
}

//Checks if a user can add a new transaction
bool canAddTransaction(const User& user) {
    return user.transactionCount < 30;
}
//Adds a new transaction to the user's history
void addTransaction(User& user, const Transaction& t) {
    if (!canAddTransaction(user)) {
        for (int i = 1; i < 30; i++) {
            user.transactions[i-1] = user.transactions[i];
        }
        user.transactionCount--;
    }
    user.transactions[user.transactionCount++] = t;
}

//Saves all user data to a binary file
void saveUsers() {
    ofstream file("users.dat", ios::binary);
    file.write((char*)&userCount, sizeof(userCount));
    file.write((char*)&nextAccountNumber, sizeof(nextAccountNumber));
    for (int i = 0; i < userCount; i++) {
        file.write((char*)&users[i], sizeof(User));
    }
    file.close();
}

//Loads all user data from a binary file
void loadUsers() {
    ifstream file("users.dat", ios::binary);
    if (!file) {
        users = new User[userCapacity];
        return;
    }

    file.read((char*)&userCount, sizeof(userCount));
    file.read((char*)&nextAccountNumber, sizeof(nextAccountNumber));
    if (userCount > userCapacity) {
        userCapacity = userCount + 2;
        users = new User[userCapacity];
    } else {
        users = new User[userCapacity];
    }

    for (int i = 0; i < userCount; i++) {
        file.read((char*)&users[i], sizeof(User));
    }
    file.close();
}

//check and validate the phone number if they where digit or not
bool isValidPhone(const char* phone) {
    for (int i = 0; phone[i] != '\0'; i++) {
        if (!isdigit(phone[i])) {
            return false;
        }
    }
    return true;
}

//This function will hide the password field(screen)
string getHiddenPassword() {
    string password;
    char ch;
    while ((ch = _getch()) != '\r') {  // Enter key is '\r'
        if (ch == '\b') { // Handle backspace
            if (!password.empty()) {
                password.pop_back();
                cout << "\b \b";
            }
        } else {
            password += ch;
            cout << '*';
        }
    }
    cout << endl;
    return password;
}

//This allows a new user to register a new account
void createAccount() {
    clearScreen();
    if (userCount >= userCapacity) {
        resizeUsersArray();
    }
    
    User newUser;
    newUser.transactionCount = 0;
    newUser.balance = 0;
    newUser.accountNumber = nextAccountNumber++;

    cout << "Enter Full Name: ";
    cin.getline(newUser.username, 50);

     if (strlen(newUser.username) == 0) {
        cout << "Error: Name cannot be empty!\n";
        system("pause");
        return;
    }

    cout << "Enter Password: ";
    string tempPassword = getHiddenPassword();
    strncpy(newUser.password, tempPassword.c_str(), sizeof(newUser.password) - 1);
    newUser.password[sizeof(newUser.password) - 1] = '\0'; // Ensure null-terminated


    bool validPhone = false;
    while (!validPhone) {
        cout << "Enter Phone Number (digits only): ";
        cin.getline(newUser.phone, 15);
        validPhone = isValidPhone(newUser.phone);
        if (!validPhone) {
            cout << "Invalid phone number! Only digits allowed.\n";
        }
    }

    cout << "Enter Initial Balance: ";
    cin >> newUser.balance;

    if (cin.fail() || newUser.balance < 0) {
        cin.clear();
        cin.ignore(numeric_limits<streamsize>::max(), '\n');
        cout << "Invalid balance amount! Account created with $0 balance.\n";
        newUser.balance = 0;
        system("pause");
    }

    users[userCount++] = newUser;
    saveUsers();
    cout << "Account Created Successfully!\n";
    cout << "=============================================================\n";
    cout << "Your account number is: " << newUser.accountNumber << " Don't forget your account\n";
    cout << "=============================================================\n";
    system("pause");
}

//Authenticates a user and returns their index
int login() {
    clearScreen();
    
    int accountNumber;
    char password[20];
    cout << "Enter Account Number: ";
    cin >> accountNumber;
    
    if (accountNumber <= 0) {
        cout << "Invalid account number!\n";
        system("pause");
        return -1;
    }

    cout << "Enter Password: ";
    string tempPassword = getHiddenPassword();
    strncpy(password, tempPassword.c_str(), sizeof(password) - 1);
    password[sizeof(password) - 1] = '\0';

    for (int i = 0; i < userCount; i++) {
        if (users[i].accountNumber == accountNumber && 
            strcmp(users[i].password, password) == 0) {
            cout << "Login Successful!\n";
            system("pause");
            return i;
        }
    }

    cout << "Invalid credentials!\n";
    system("pause");
    return -1;
}
void performTransfer(int senderIndex, int receiverAccount, double amount) {
    std::lock_guard<std::mutex> lock(transferMutex);

    if (amount <= 0 || amount > users[senderIndex].balance) {
        cout << "Invalid or insufficient amount!\n";
        return;
    }

    if (users[senderIndex].accountNumber == receiverAccount) {
        cout << "Cannot transfer to the same account!\n";
        return;
    }

    int receiverIndex = -1;
    for (int i = 0; i < userCount; i++) {
        if (users[i].accountNumber == receiverAccount) {
            receiverIndex = i;
            break;
        }
    }

    if (receiverIndex == -1) {
        cout << "Receiver not found!\n";
        return;
    }

    users[senderIndex].balance -= amount;
    users[receiverIndex].balance += amount;

    Transaction t;
    getCurrentDateTime(t.date, t.time);
    strcpy(t.type, "Transfer");
    t.amount = amount;
    strcpy(t.sender, to_string(users[senderIndex].accountNumber).c_str());
    strcpy(t.receiver, to_string(users[receiverIndex].accountNumber).c_str());

    addTransaction(users[senderIndex], t);

    Transaction t2 = t;
    strcpy(t2.type, "Received");
    addTransaction(users[receiverIndex], t2);

    saveUsers();

    cout << "Transfer from " << users[senderIndex].accountNumber
         << " to " << receiverAccount << " of $" << amount << " completed.\n";
}

void deposit(int userIndex) {
    clearScreen();
    double amount;
    cout << "Current Balance: $" << users[userIndex].balance << "\n";
    cout << "Enter deposit amount: ";
    cin >> amount;

    if (cin.fail() || amount <= 0) {
        cin.clear();
        cin.ignore(numeric_limits<streamsize>::max(), '\n');
        cout << "Invalid amount!\n";
        system("pause");
        return;
    }

    users[userIndex].balance += amount;

    Transaction t;
    getCurrentDateTime(t.date, t.time);
    strcpy(t.type, "Deposit");
    t.amount = amount;
    strcpy(t.sender, to_string(users[userIndex].accountNumber).c_str());
    strcpy(t.receiver, "-");

    addTransaction(users[userIndex], t);
    saveUsers();
    cout << "Deposit Successful! New Balance: $" << users[userIndex].balance << "\n";
    system("pause");
}

void withdraw(int userIndex) {
    clearScreen();
    double amount;
    cout << "Current Balance: $" << users[userIndex].balance << "\n";
    cout << "Enter withdrawal amount: ";
    cin >> amount;

    if (cin.fail() || amount <= 0 || amount > users[userIndex].balance) {
        cin.clear();
        cin.ignore(numeric_limits<streamsize>::max(), '\n');
        cout << "Invalid amount!\n";
        system("pause");
        return;
    }

    users[userIndex].balance -= amount;

    Transaction t;
    getCurrentDateTime(t.date, t.time);
    strcpy(t.type, "Withdraw");
    t.amount = amount;
    strcpy(t.sender, to_string(users[userIndex].accountNumber).c_str());
    strcpy(t.receiver, "-");

    addTransaction(users[userIndex], t);
    saveUsers();
    cout << "Withdrawal Successful! New Balance: $" << users[userIndex].balance << "\n";
    system("pause");
}

// handles money tranfer between two users.
void transfer(int userIndex) {
    clearScreen();
    int receiverAccount;
    double amount;
    
    cout << "Current Balance: $" << users[userIndex].balance << "\n";
    cout << "Enter recipient's account number: ";
    cin >> receiverAccount;
    
    cout << "Enter amount to transfer: ";
    cin >> amount;

    if (cin.fail()) {
        cin.clear();
        cin.ignore(numeric_limits<streamsize>::max(), '\n');
        cout << "Invalid input!\n";
        system("pause");
        return;
    }

    thread t1(performTransfer, userIndex, receiverAccount, amount);
    t1.join(); // Waits for the thread to finish (safe for now)

    system("pause");
}

//customer profile formate function.
void viewTransactionHistory(int userIndex) {
    clearScreen();
    cout << "===== TRANSACTION HISTORY =====\n";
    cout << "Account: " << users[userIndex].accountNumber << "\n";
    cout << "Date       | Time     | Type      | Amount  | Details\n";
    cout << "----------------------------------------------------\n";

    for (int i = 0; i < users[userIndex].transactionCount; i++) {
        Transaction t = users[userIndex].transactions[i];
        cout << t.date << " | " << t.time << " | " 
             << setw(9) << left << t.type << " | $" << setw(7) << left << t.amount;
        
        if (strcmp(t.type, "Transfer") == 0) {
            cout << "To: " << t.receiver;
        } else if (strcmp(t.type, "Received") == 0) {
            cout << "From: " << t.sender;
        }
        cout << endl;
    }
    system("pause");
}

//Printable transactions histroy exporter function
void exportUserDataToText(int userIndex) {
    ofstream textFile("customer_profile.txt");
    
    textFile << "===== CUSTOMER PROFILE =====\n";
    textFile << "Account Number: " << users[userIndex].accountNumber << "\n";
    textFile << "Customer Name: " << users[userIndex].username << "\n";
    textFile << "Phone Number: " << users[userIndex].phone << "\n";
    textFile << "Current Balance: $" << users[userIndex].balance << "\n\n";
    
    textFile << "===== TRANSACTION HISTORY =====\n";
    textFile << "Date       | Time     | Type      | Amount  | Details\n";
    textFile << "----------------------------------------------------\n";
    
    for (int i = 0; i < users[userIndex].transactionCount; i++) {
        Transaction t = users[userIndex].transactions[i];
        textFile << t.date << " | " << t.time << " | " 
                 << setw(9) << left << t.type << " | $" << setw(7) << left << t.amount;
        
        if (strcmp(t.type, "Transfer") == 0) {
            textFile << "To: " << t.receiver;
        } else if (strcmp(t.type, "Received") == 0) {
            textFile << "From: " << t.sender;
        }
        textFile << endl;
    }
    
    textFile.close();
    cout << "Data exported to customer_profile.txt (editable/printable)\n";
    system("pause");
}
//CSV file exporter for accounting purposes 
void exportToCSV(int userIndex) {
    ofstream csvFile("transactions.csv");
    
    csvFile << "Date,Time,Type,Amount,Sender,Receiver\n";
    
    for (int i = 0; i < users[userIndex].transactionCount; i++) {
        Transaction t = users[userIndex].transactions[i];
        csvFile << t.date << "," << t.time << "," << t.type << ","
                << t.amount << "," << t.sender << "," << t.receiver << "\n";
    }
    
    csvFile.close();
    cout << "Transaction history exported to transactions.csv\n";
    system("pause");
}
// Transaction menu 
void transactionsMenu(int userIndex) {
    int choice;
    do {
        clearScreen();
        displayProjectInfo();
        cout << "\t\tWelcome, " << users[userIndex].username << "!\n";
        cout << "Account Number: " << users[userIndex].accountNumber << "\t";
        cout << "Current Balance: $" << users[userIndex].balance << "\n\n";
        cout << "\t===== TRANSACTION MENU =====\n";
        cout << "\t\t1. Deposit\n";
        cout << "\t\t2. Withdraw\n";
        cout << "\t\t3. Transfer\n";
        cout << "\t\t4. View Transaction History\n";
        cout << "\t\t5. Export Profile (Text)\n";
        cout << "\t\t6. Export Transactions (CSV)\n";
        cout << "\t\t7. Logout\n";
        cout << "\tEnter your choice: ";
        
        cin >> choice;
        cin.ignore(numeric_limits<streamsize>::max(), '\n');
        
        if (cin.fail()) {
            cin.clear();
            cin.ignore(numeric_limits<streamsize>::max(), '\n');
            cout << "Invalid input!\n";
            system("pause");
            continue;
        }

        switch(choice) {
            case 1: deposit(userIndex); break;
            case 2: withdraw(userIndex); break;
            case 3: transfer(userIndex); break;
            case 4: viewTransactionHistory(userIndex); break;
            case 5: exportUserDataToText(userIndex); break;
            case 6: exportToCSV(userIndex); break;
            case 7: return;
            default:
                cout << "Invalid choice! Try again.\n";
                system("pause");
        }
    } while (true);
}

//Displays the main menu and handles navigation
int main() {
    users = new User[userCapacity];
    loadUsers();
    
    while (true) {
        clearScreen();
        displayProjectInfo();
        cout << "\t===== TAM BANKING SYSTEM =====\n";
        cout << "\t\t1. Create Account\n";
        cout << "\t\t2. Login\n";
        cout << "\t\t3. Exit\n";
        cout << "\t==============================\n";
        cout << "\tEnter your choice: ";

        int choice;
        cin >> choice;
        cin.ignore(numeric_limits<streamsize>::max(), '\n');

        if (cin.fail()) {
            cin.clear();
            cin.ignore(numeric_limits<streamsize>::max(), '\n');
            cout << "Invalid input! Try again.\n";
            system("pause");
            continue;
        }

        switch (choice) {
            case 1: createAccount(); break;
            case 2: {
                int userIndex = login();
                if (userIndex != -1) {
                    transactionsMenu(userIndex);
                }
                break;
            }
            case 3:
                saveUsers();
                delete[] users;
                cout << "Exiting...\n";
                return 0;
            default:
                cout << "Invalid choice! Try again.\n";
                system("pause");
        }
    }
}