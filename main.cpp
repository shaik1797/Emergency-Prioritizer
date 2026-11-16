/**
 * ============================================================================
 * LORDS INSTITUTE OF ENGINEERING & TECHNOLOGY (AUTONOMOUS)
 * Department of Computer Science & Engineering (AI & ML)
 * Data Structures & Algorithms (Assignment-III / Micro-Project #17)
 * ----------------------------------------------------------------------------
 * Project Title : Emergency Service Request Prioritizer
 * Architecture  : Hand-Written Binary Max-Heap with Index Mapping + Qt GUI
 * Language      : C++17
 * Platform      : Cross-Platform (Windows MinGW / Linux GCC / macOS Clang)
 * Target Machine: Intel Core i3 / 4 GB RAM friendly (zero runtime dependencies)
 * ============================================================================
 */

#include <QApplication>
#include <QMainWindow>
#include <QTabWidget>
#include <QWidget>
#include <QVBoxLayout>
#include <QHBoxLayout>
#include <QFormLayout>
#include <QLabel>
#include <QLineEdit>
#include <QComboBox>
#include <QTextEdit>
#include <QPushButton>
#include <QTableWidget>
#include <QTableWidgetItem>
#include <QHeaderView>
#include <QMessageBox>
#include <QStatusBar>
#include <QMenuBar>
#include <QMenu>
#include <QAction>
#include <QFileDialog>
#include <QPainter>
#include <QPainterPath>
#include <QScrollArea>
#include <QTimer>
#include <QDateTime>
#include <QRegularExpression>
#include <QInputDialog>
#include <QColor>
#include <QFont>
#include <QPen>
#include <QBrush>
#include <QFile>
#include <QTextStream>

#include <vector>
#include <string>
#include <unordered_map>
#include <algorithm>
#include <cmath>
#include <utility>

// ============================================================================
// 1. DATA MODEL: EmergencyRequest
// ============================================================================

enum class SeverityLevel {
    Low = 1,
    Moderate = 2,
    High = 3,
    Critical = 4
};

enum class RequestStatus {
    Pending,
    InProgress,
    Completed,
    Cancelled
};

struct EmergencyRequest {
    std::string id;             // Auto-generated Unique ID (e.g., REQ-1001)
    QString callerName;         // Name of the reporting person
    QString phoneNumber;        // 10-digit contact number
    QString location;           // Incident address / landmark
    QString serviceType;        // Ambulance, Fire, Police, Disaster Relief, Other
    int severity;               // 1: Low, 2: Moderate, 3: High, 4: Critical
    QString description;        // Details of the emergency
    uint64_t sequenceNumber;    // Auto-incremented arrival counter for FIFO tie-breaking
    QString timestamp;          // ISO format timestamp
    RequestStatus status;       // Current status of the request
    QString resolutionTime;     // Timestamp when dispatched or cancelled

    EmergencyRequest()
        : severity(1), sequenceNumber(0), status(RequestStatus::Pending) {}

    // Severity string helper
    static QString severityToString(int level) {
        switch (level) {
            case 4: return "4 - Critical";
            case 3: return "3 - High";
            case 2: return "2 - Moderate";
            case 1: default: return "1 - Low";
        }
    }

    // Status string helper
    static QString statusToString(RequestStatus s) {
        switch (s) {
            case RequestStatus::Pending:    return "Pending";
            case RequestStatus::InProgress: return "In Progress";
            case RequestStatus::Completed:  return "Completed (Dispatched)";
            case RequestStatus::Cancelled:  return "Cancelled";
            default:                        return "Unknown";
        }
    }
};

// ============================================================================
// 2. DATA STRUCTURE: Hand-Written Binary Max-Heap (Dynamic Array)
// ============================================================================
/**
 * Priority Criteria:
 *   Primary   : Severity (Higher number = Higher priority)
 *   Secondary : Arrival Sequence (Lower number = Earlier arrival = FIFO tie-breaker)
 *
 * Underlying storage: std::vector<EmergencyRequest> heap;
 * Auxiliary index   : std::unordered_map<std::string, size_t> idToIndex;
 *
 * Array Indexing Rules (0-based):
 *   Parent(i)      = (i - 1) / 2
 *   LeftChild(i)   = 2 * i + 1
 *   RightChild(i)  = 2 * i + 2
 */
class EmergencyMaxHeap {
private:
    std::vector<EmergencyRequest> heap;
    std::unordered_map<std::string, size_t> idToIndex;

    // Instrumentation metrics for analysis
    mutable size_t lastComparisons = 0;
    mutable size_t lastSwaps = 0;
    std::string lastOperationName = "None";

    // For Visualizer node swap animations
    std::vector<std::pair<size_t, size_t>> recordedSwaps;

    /**
     * Comparator: returns true if request 'a' strictly has higher priority than 'b'.
     * Time Complexity: O(1)
     */
    bool hasHigherPriority(const EmergencyRequest& a, const EmergencyRequest& b) const {
        lastComparisons++;
        if (a.severity != b.severity) {
            return a.severity > b.severity; // Higher severity wins
        }
        // Tie-breaker: earlier arrival (lower sequence number) wins (FIFO)
        return a.sequenceNumber < b.sequenceNumber;
    }

    /**
     * Swaps two nodes in the heap and keeps the idToIndex map synchronized.
     * Time Complexity: O(1)
     */
    void swapNodes(size_t i, size_t j) {
        lastSwaps++;
        recordedSwaps.push_back({i, j});
        std::swap(heap[i], heap[j]);
        idToIndex[heap[i].id] = i;
        idToIndex[heap[j].id] = j;
    }

    /**
     * Restores max-heap property upwards from index i.
     * Time Complexity: O(log n)
     */
    void siftUp(size_t i) {
        while (i > 0) {
            size_t parent = (i - 1) / 2;
            if (hasHigherPriority(heap[i], heap[parent])) {
                swapNodes(i, parent);
                i = parent;
            } else {
                break;
            }
        }
    }

    /**
     * Restores max-heap property downwards from index i.
     * Time Complexity: O(log n)
     */
    void siftDown(size_t i) {
        size_t n = heap.size();
        while (true) {
            size_t left = 2 * i + 1;
            size_t right = 2 * i + 2;
            size_t largest = i;

            if (left < n && hasHigherPriority(heap[left], heap[largest])) {
                largest = left;
            }
            if (right < n && hasHigherPriority(heap[right], heap[largest])) {
                largest = right;
            }

            if (largest != i) {
                swapNodes(i, largest);
                i = largest;
            } else {
                break;
            }
        }
    }

public:
    EmergencyMaxHeap() = default;

    void resetMetrics(const std::string& opName) {
        lastComparisons = 0;
        lastSwaps = 0;
        lastOperationName = opName;
        recordedSwaps.clear();
    }

    size_t getLastComparisons() const { return lastComparisons; }
    size_t getLastSwaps() const { return lastSwaps; }
    std::string getLastOperationName() const { return lastOperationName; }
    const std::vector<std::pair<size_t, size_t>>& getRecordedSwaps() const { return recordedSwaps; }

    bool empty() const { return heap.empty(); }
    size_t size() const { return heap.size(); }

    void clear() {
        heap.clear();
        idToIndex.clear();
        resetMetrics("Clear");
    }

    /**
     * Peek at the highest-priority request without removing it.
     * Time Complexity: O(1)
     */
    bool peek(EmergencyRequest& outRequest) const {
        resetMetrics("Peek");
        if (heap.empty()) return false;
        outRequest = heap[0];
        return true;
    }

    /**
     * Insert a new emergency request into the max-heap.
     * Time Complexity: O(log n)
     * Space Complexity: O(1) auxiliary
     */
    void insert(const EmergencyRequest& req) {
        resetMetrics("Insert (" + req.id + ")");
        heap.push_back(req);
        size_t idx = heap.size() - 1;
        idToIndex[req.id] = idx;
        siftUp(idx);
    }

    /**
     * Extract the highest priority request (Dispatch Next).
     * Time Complexity: O(log n)
     * Space Complexity: O(1) auxiliary
     */
    bool extractMax(EmergencyRequest& outRequest) {
        resetMetrics("Extract-Max / Dispatch");
        if (heap.empty()) return false;

        outRequest = heap[0];
        idToIndex.erase(outRequest.id);

        if (heap.size() == 1) {
            heap.pop_back();
            return true;
        }

        // Move last element to root and sift down
        heap[0] = heap.back();
        idToIndex[heap[0].id] = 0;
        heap.pop_back();
        siftDown(0);

        return true;
    }

    /**
     * Updates the severity of an existing request and re-heapifies.
     * Time Complexity: O(log n) via O(1) lookup
     * Space Complexity: O(1) auxiliary
     */
    bool changeSeverity(const std::string& id, int newSeverity) {
        resetMetrics("Update Severity (" + id + ")");
        auto it = idToIndex.find(id);
        if (it == idToIndex.end()) return false;

        size_t idx = it->second;
        int oldSeverity = heap[idx].severity;
        heap[idx].severity = newSeverity;

        if (newSeverity > oldSeverity) {
            // Priority increased -> sift up
            siftUp(idx);
        } else if (newSeverity < oldSeverity) {
            // Priority decreased -> sift down
            siftDown(idx);
        }
        return true;
    }

    /**
     * Remove / cancel a request by ID.
     * Time Complexity: O(log n)
     * Space Complexity: O(1) auxiliary
     */
    bool removeById(const std::string& id, EmergencyRequest& outRemoved) {
        resetMetrics("Cancel/Remove (" + id + ")");
        auto it = idToIndex.find(id);
        if (it == idToIndex.end()) return false;

        size_t idx = it->second;
        outRemoved = heap[idx];
        idToIndex.erase(id);

        size_t lastIdx = heap.size() - 1;
        if (idx == lastIdx) {
            heap.pop_back();
            return true;
        }

        // Replace node with last element
        EmergencyRequest moved = heap[lastIdx];
        heap[idx] = moved;
        idToIndex[moved.id] = idx;
        heap.pop_back();

        // Re-heapify: determine whether to sift up or sift down
        size_t parent = (idx > 0) ? (idx - 1) / 2 : 0;
        if (idx > 0 && hasHigherPriority(heap[idx], heap[parent])) {
            siftUp(idx);
        } else {
            siftDown(idx);
        }
        return true;
    }

    /**
     * Search by ID, caller name, or location.
     * Time Complexity: O(n) linear scan
     */
    std::vector<EmergencyRequest> search(const QString& query) const {
        resetMetrics("Search: " + query.toStdString());
        std::vector<EmergencyRequest> results;
        QString q = query.trimmed().toLower();
        if (q.isEmpty()) return heap;

        for (const auto& req : heap) {
            lastComparisons++;
            if (QString::fromStdString(req.id).toLower().contains(q) ||
                req.callerName.toLower().contains(q) ||
                req.location.toLower().contains(q) ||
                req.serviceType.toLower().contains(q) ||
                req.phoneNumber.contains(q)) {
                results.push_back(req);
            }
        }
        return results;
    }

    /**
     * Build-Heap algorithm from an existing list of requests (e.g. on file load).
     * Time Complexity: O(n) bottom-up heapification
     * Space Complexity: O(1) auxiliary
     */
    void buildHeap(const std::vector<EmergencyRequest>& items) {
        resetMetrics("Build-Heap (Bottom-Up)");
        heap = items;
        idToIndex.clear();
        for (size_t i = 0; i < heap.size(); ++i) {
            idToIndex[heap[i].id] = i;
        }

        if (heap.empty()) return;

        // Bottom-up heapify: start from the last non-leaf node down to 0
        int n = static_cast<int>(heap.size());
        for (int i = (n / 2) - 1; i >= 0; --i) {
            siftDown(static_cast<size_t>(i));
        }
    }

    /**
     * Returns all pending requests sorted in full priority order without destroying original heap.
     * Uses Heap Sort algorithm on a deep copy of the underlying vector.
     * Time Complexity: O(n log n)
     * Space Complexity: O(n) auxiliary
     */
    std::vector<EmergencyRequest> getSortedRequests() const {
        std::vector<EmergencyRequest> copy = heap;
        std::vector<EmergencyRequest> sortedList;
        sortedList.reserve(copy.size());

        // Create temporary heap from copy
        EmergencyMaxHeap tempHeap;
        tempHeap.buildHeap(copy);

        EmergencyRequest top;
        while (tempHeap.extractMax(top)) {
            sortedList.push_back(top);
        }
        return sortedList;
    }

    // Direct read-only access for visualization and data table
    const std::vector<EmergencyRequest>& getRawHeap() const { return heap; }

    bool containsId(const std::string& id) const {
        return idToIndex.find(id) != idToIndex.end();
    }

    const EmergencyRequest* getById(const std::string& id) const {
        auto it = idToIndex.find(id);
        if (it == idToIndex.end()) return nullptr;
        return &heap[it->second];
    }
};

// ============================================================================
// 3. CUSTOM WIDGET: HeapTreeWidget (Binary Tree Visualizer)
// ============================================================================
class HeapTreeWidget : public QWidget {
    Q_OBJECT
private:
    const EmergencyMaxHeap* heapRef;
    int highlightedIndexA;
    int highlightedIndexB;
    QTimer* animTimer;
    int animPulse;

public:
    explicit HeapTreeWidget(const EmergencyMaxHeap* heap, QWidget* parent = nullptr)
        : QWidget(parent), heapRef(heap), highlightedIndexA(-1), highlightedIndexB(-1), animPulse(0) {
        setBackgroundRole(QPalette::Base);
        setAutoFillBackground(true);
        setMinimumSize(900, 600);

        animTimer = new QTimer(this);
        connect(animTimer, &QTimer::timeout, this, [this]() {
            animPulse = (animPulse + 1) % 6;
            update();
        });
    }

    void triggerHighlight(int indexA, int indexB) {
        highlightedIndexA = indexA;
        highlightedIndexB = indexB;
        animPulse = 0;
        animTimer->start(120);
        QTimer::singleShot(1800, this, [this]() {
            animTimer->stop();
            highlightedIndexA = -1;
            highlightedIndexB = -1;
            update();
        });
        update();
    }

protected:
    void paintEvent(QPaintEvent* event) override {
        Q_UNUSED(event);
        QPainter painter(this);
        painter.setRenderHint(QPainter::Antialiasing, true);

        // Background
        painter.fillRect(rect(), QColor(248, 249, 250));

        if (!heapRef || heapRef->empty()) {
            painter.setPen(QColor(120, 144, 156));
            painter.setFont(QFont("Segoe UI", 14, QFont::Bold));
            painter.drawText(rect(), Qt::AlignCenter, "Priority Queue is currently empty.\nNo active emergency nodes to visualize.");
            return;
        }

        const auto& nodes = heapRef->getRawHeap();
        size_t n = nodes.size();

        // Calculate positions level-by-level
        // Node diameter
        const int nodeRadius = 24;
        const int levelHeight = 85;

        // Map index to center QPoint
        std::vector<QPoint> coords(n);

        int totalLevels = static_cast<int>(std::floor(std::log2(n))) + 1;
        int totalWidth = std::max(width(), 1 << (totalLevels - 1) * 70);

        for (size_t i = 0; i < n; ++i) {
            int level = static_cast<int>(std::floor(std::log2(i + 1)));
            int indexInLevel = static_cast<int>(i - ((1 << level) - 1));
            int nodesInThisLevel = 1 << level;

            int segmentWidth = totalWidth / nodesInThisLevel;
            int x = (indexInLevel * segmentWidth) + (segmentWidth / 2);
            int y = 45 + (level * levelHeight);

            coords[i] = QPoint(x, y);
        }

        // Adjust widget size if tree is wide
        if (coords.back().y + 80 > height() || totalWidth > width()) {
            setMinimumSize(std::max(totalWidth, 900), coords.back().y + 100);
        }

        // 1. Draw connecting branches (Edges)
        QPen branchPen(QColor(180, 190, 200), 2, Qt::SolidLine, Qt::RoundCap);
        painter.setPen(branchPen);

        for (size_t i = 0; i < n; ++i) {
            size_t left = 2 * i + 1;
            size_t right = 2 * i + 2;

            if (left < n) {
                painter.drawLine(coords[i], coords[left]);
            }
            if (right < n) {
                painter.drawLine(coords[i], coords[right]);
            }
        }

        // 2. Draw Nodes
        for (size_t i = 0; i < n; ++i) {
            QPoint center = coords[i];
            const auto& req = nodes[i];

            // Color scheme based on severity
            QColor nodeColor;
            QColor borderColor(40, 50, 60);

            switch (req.severity) {
                case 4: // Critical - Vibrant Red
                    nodeColor = QColor(229, 57, 53);
                    break;
                case 3: // High - Deep Orange
                    nodeColor = QColor(251, 140, 0);
                    break;
                case 2: // Moderate - Amber / Golden Yellow
                    nodeColor = QColor(253, 216, 53);
                    break;
                case 1: // Low - Fresh Green
                default:
                    nodeColor = QColor(67, 160, 71);
                    break;
            }

            // Highlighting animation for swapped or active nodes
            bool isHighlighted = (static_cast<int>(i) == highlightedIndexA || static_cast<int>(i) == highlightedIndexB);
            if (isHighlighted) {
                QColor glowColor(33, 150, 243, 160);
                painter.setPen(Qt::NoPen);
                painter.setBrush(glowColor);
                int glowRadius = nodeRadius + 6 + (animPulse % 4) * 2;
                painter.drawEllipse(center, glowRadius, glowRadius);
            }

            // Draw Node Circle
            QPen nodeBorder(isHighlighted ? QColor(21, 101, 192) : borderColor, isHighlighted ? 3 : 1.5);
            painter.setPen(nodeBorder);
            painter.setBrush(nodeColor);
            painter.drawEllipse(center, nodeRadius, nodeRadius);

            // Node Text (ID & Severity)
            painter.setPen(req.severity == 2 ? Qt::black : Qt::white);
            painter.setFont(QFont("Segoe UI", 8, QFont::Bold));
            painter.drawText(QRect(center.x() - nodeRadius, center.y() - 15, nodeRadius * 2, 14),
                             Qt::AlignCenter, QString::fromStdString(req.id));

            painter.setFont(QFont("Segoe UI", 7, QFont::Normal));
            QString meta = QString("S%1 #%2").arg(req.severity).arg(req.sequenceNumber);
            painter.drawText(QRect(center.x() - nodeRadius, center.y() + 1, nodeRadius * 2, 13),
                             Qt::AlignCenter, meta);

            // Index tag below node
            painter.setPen(QColor(100, 116, 139));
            painter.setFont(QFont("Consolas", 7));
            painter.drawText(QRect(center.x() - 20, center.y() + nodeRadius + 2, 40, 12),
                             Qt::AlignCenter, QString("[%1]").arg(i));
        }

        // Legend at bottom left
        painter.resetTransform();
        painter.setFont(QFont("Segoe UI", 8));
        int legY = 15;
        auto drawLegendItem = [&](const QString& label, const QColor& col, int x) {
            painter.setPen(Qt::NoPen);
            painter.setBrush(col);
            painter.drawRoundedRect(x, legY, 12, 12, 3, 3);
            painter.setPen(QColor(50, 50, 50));
            painter.drawText(x + 18, legY + 10, label);
        };
        drawLegendItem("Critical (4)", QColor(229, 57, 53), 20);
        drawLegendItem("High (3)", QColor(251, 140, 0), 120);
        drawLegendItem("Moderate (2)", QColor(253, 216, 53), 200);
        drawLegendItem("Low (1)", QColor(67, 160, 71), 310);
    }
};

// ============================================================================
// 4. MAIN APPLICATION WINDOW: MainWindow
// ============================================================================
class MainWindow : public QMainWindow {
    Q_OBJECT

private:
    EmergencyMaxHeap heap;
    std::vector<EmergencyRequest> history; // Queue / List of resolved requests
    uint64_t nextSequenceNumber = 1001;

    // UI Widgets
    QTabWidget* tabWidget;

    // Tab 1: New Request Form
    QLineEdit* editCallerName;
    QLineEdit* editPhone;
    QLineEdit* editLocation;
    QComboBox* comboService;
    QComboBox* comboSeverity;
    QTextEdit* editDescription;

    // Tab 2: Priority Queue Table & Controls
    QTableWidget* queueTable;
    QLineEdit* searchBox;
    QPushButton* btnDispatchNext;
    QPushButton* btnChangeSeverity;
    QPushButton* btnCancelRequest;

    // Tab 3: Heap Visualizer
    HeapTreeWidget* visualizerWidget;
    QScrollArea* visualizerScroll;

    // Tab 4: History Table
    QTableWidget* historyTable;

    // Tab 5: Analysis
    QTableWidget* complexityTable;
    QLabel* lblLastOperation;
    QLabel* lblComparisons;
    QLabel* lblSwaps;
    QLabel* lblCurrentHeapSize;
    QLabel* lblTreeHeight;

public:
    MainWindow(QWidget* parent = nullptr) : QMainWindow(parent) {
        setWindowTitle("Emergency Service Request Prioritizer - DSA Assignment #17");
        resize(1120, 750);
        setMinimumSize(850, 600);

        setupMenuBar();
        setupTabs();
        setupStatusBar();
        refreshAllViews();
    }

private:
    // ------------------------------------------------------------------------
    // UI SETUP: Menu Bar
    // ------------------------------------------------------------------------
    void setupMenuBar() {
        QMenuBar* bar = menuBar();

        // File Menu
        QMenu* fileMenu = bar->addMenu("&File");
        QAction* actSave = fileMenu->addAction("&Save Requests to CSV...");
        actSave->setShortcut(QKeySequence::Save);
        connect(actSave, &QAction::triggered, this, &MainWindow::onSaveToFile);

        QAction* actLoad = fileMenu->addAction("&Load Requests from CSV...");
        actLoad->setShortcut(QKeySequence::Open);
        connect(actLoad, &QAction::triggered, this, &MainWindow::onLoadFromFile);

        fileMenu->addSeparator();
        QAction* actDemo = fileMenu->addAction("Load 10 &Demo Requests");
        connect(actDemo, &QAction::triggered, this, &MainWindow::loadDemoData);

        fileMenu->addSeparator();
        QAction* actExit = fileMenu->addAction("E&xit");
        actExit->setShortcut(QKeySequence::Quit);
        connect(actExit, &QAction::triggered, this, &QWidget::close);

        // Help Menu
        QMenu* helpMenu = bar->addMenu("&Help");
        QAction* actAbout = helpMenu->addAction("&About Project");
        connect(actAbout, &QAction::triggered, this, &MainWindow::showAboutDialog);
    }

    // ------------------------------------------------------------------------
    // UI SETUP: Tabs
    // ------------------------------------------------------------------------
    void setupTabs() {
        tabWidget = new QTabWidget(this);
        setCentralWidget(tabWidget);

        tabWidget->addTab(createTabNewRequest(), "➕ New Request");
        tabWidget->addTab(createTabPriorityQueue(), "⚡ Priority Queue");
        tabWidget->addTab(createTabVisualizer(), "🌳 Heap Visualizer");
        tabWidget->addTab(createTabHistory(), "📜 History");
        tabWidget->addTab(createTabAnalysis(), "📊 Analysis & Complexity");

        connect(tabWidget, &QTabWidget::currentChanged, this, [this](int idx) {
            if (idx == 2 && visualizerWidget) {
                visualizerWidget->update();
            }
        });
    }

    // ------------------------------------------------------------------------
    // TAB 1: New Request Form
    // ------------------------------------------------------------------------
    QWidget* createTabNewRequest() {
        QWidget* page = new QWidget();
        QVBoxLayout* rootLayout = new QVBoxLayout(page);
        rootLayout->setContentsMargins(30, 25, 30, 25);
        rootLayout->setSpacing(15);

        QLabel* header = new QLabel("Intake New Emergency Service Request");
        header->setStyleSheet("font-size: 18px; font-weight: bold; color: #1a237e;");
        rootLayout->addWidget(header);

        QLabel* subtitle = new QLabel("Incoming calls are classified by severity and automatically positioned in the Max-Heap priority queue.");
        subtitle->setStyleSheet("color: #546e7a; margin-bottom: 10px;");
        rootLayout->addWidget(subtitle);

        QFormLayout* form = new QFormLayout();
        form->setLabelAlignment(Qt::AlignRight);
        form->setFieldGrowthPolicy(QFormLayout::ExpandingFieldsGrow);
        form->setSpacing(12);

        editCallerName = new QLineEdit();
        editCallerName->setPlaceholderText("Full Name of caller (e.g. Rahul Sharma)");
        form->addRow("<b>Caller Name:</b>", editCallerName);

        editPhone = new QLineEdit();
        editPhone->setPlaceholderText("10-digit mobile number (e.g. 9876543210)");
        form->addRow("<b>Phone Number:</b>", editPhone);

        editLocation = new QLineEdit();
        editLocation->setPlaceholderText("Accident location or address (e.g. NH-44, Pillar 102)");
        form->addRow("<b>Location:</b>", editLocation);

        comboService = new QComboBox();
        comboService->addItems({"Ambulance", "Fire", "Police", "Disaster Relief", "Other"});
        form->addRow("<b>Service Required:</b>", comboService);

        comboSeverity = new QComboBox();
        comboSeverity->addItem("1 - Low (Routine inquiry / non-urgent aid)", 1);
        comboSeverity->addItem("2 - Moderate (Minor injury / property dispute)", 2);
        comboSeverity->addItem("3 - High (Severe injury / structural fire)", 3);
        comboSeverity->addItem("4 - Critical (Cardiac arrest / multi-vehicle collision)", 4);
        comboSeverity->setCurrentIndex(2); // Default to High
        form->addRow("<b>Severity Level:</b>", comboSeverity);

        editDescription = new QTextEdit();
        editDescription->setPlaceholderText("Detailed situation description and instructions for first responders...");
        editDescription->setMaximumHeight(90);
        form->addRow("<b>Description:</b>", editDescription);

        rootLayout->addLayout(form);

        QHBoxLayout* btnLayout = new QHBoxLayout();
        btnLayout->addStretch();

        QPushButton* btnDemo = new QPushButton("Load 10 Demo Requests");
        btnDemo->setStyleSheet("padding: 9px 18px; font-weight: bold; background-color: #eceff1; border-radius: 4px;");
        connect(btnDemo, &QPushButton::clicked, this, &MainWindow::loadDemoData);
        btnLayout->addWidget(btnDemo);

        QPushButton* btnSubmit = new QPushButton("Queue Emergency Request (Insert)");
        btnSubmit->setStyleSheet("padding: 9px 24px; font-weight: bold; background-color: #d32f2f; color: white; border-radius: 4px;");
        connect(btnSubmit, &QPushButton::clicked, this, &MainWindow::onSubmitNewRequest);
        btnLayout->addWidget(btnSubmit);

        rootLayout->addLayout(btnLayout);
        rootLayout->addStretch();
        return page;
    }

    // ------------------------------------------------------------------------
    // TAB 2: Priority Queue (Table, Search, Actions)
    // ------------------------------------------------------------------------
    QWidget* createTabPriorityQueue() {
        QWidget* page = new QWidget();
        QVBoxLayout* layout = new QVBoxLayout(page);
        layout->setContentsMargins(15, 15, 15, 15);
        layout->setSpacing(10);

        // Action Toolbar
        QHBoxLayout* topBar = new QHBoxLayout();

        btnDispatchNext = new QPushButton("🚑 Dispatch Next (Extract Max)");
        btnDispatchNext->setStyleSheet("background-color: #2e7d32; color: white; font-weight: bold; padding: 8px 16px; border-radius: 4px;");
        connect(btnDispatchNext, &QPushButton::clicked, this, &MainWindow::onDispatchNext);
        topBar->addWidget(btnDispatchNext);

        btnChangeSeverity = new QPushButton("✏️ Change Severity");
        btnChangeSeverity->setStyleSheet("background-color: #0288d1; color: white; font-weight: bold; padding: 8px 16px; border-radius: 4px;");
        connect(btnChangeSeverity, &QPushButton::clicked, this, &MainWindow::onChangeSeverityClicked);
        topBar->addWidget(btnChangeSeverity);

        btnCancelRequest = new QPushButton("❌ Cancel Request");
        btnCancelRequest->setStyleSheet("background-color: #c62828; color: white; font-weight: bold; padding: 8px 16px; border-radius: 4px;");
        connect(btnCancelRequest, &QPushButton::clicked, this, &MainWindow::onCancelRequestClicked);
        topBar->addWidget(btnCancelRequest);

        topBar->addStretch();

        QLabel* lblSearch = new QLabel("Search:");
        topBar->addWidget(lblSearch);
        searchBox = new QLineEdit();
        searchBox->setPlaceholderText("Filter by ID, Caller, Location, Service...");
        searchBox->setFixedWidth(240);
        connect(searchBox, &QLineEdit::textChanged, this, &MainWindow::onSearchChanged);
        topBar->addWidget(searchBox);

        layout->addLayout(topBar);

        // Table
        queueTable = new QTableWidget();
        queueTable->setColumnCount(10);
        queueTable->setHorizontalHeaderLabels({
            "Priority Rank", "ID", "Severity", "Service", "Caller Name",
            "Phone", "Location", "Arrival #", "Timestamp", "Description"
        });
        queueTable->horizontalHeader()->setSectionResizeMode(QHeaderView::ResizeToContents);
        queueTable->horizontalHeader()->setSectionResizeMode(9, QHeaderView::Stretch);
        queueTable->setSelectionBehavior(QAbstractItemView::SelectRows);
        queueTable->setSelectionMode(QAbstractItemView::SingleSelection);
        queueTable->setAlternatingRowColors(false);
        queueTable->verticalHeader()->setVisible(false);
        layout->addWidget(queueTable);

        return page;
    }

    // ------------------------------------------------------------------------
    // TAB 3: Heap Visualizer
    // ------------------------------------------------------------------------
    QWidget* createTabVisualizer() {
        QWidget* page = new QWidget();
        QVBoxLayout* layout = new QVBoxLayout(page);
        layout->setContentsMargins(10, 10, 10, 10);

        QLabel* infoLabel = new QLabel("Binary Max-Heap Visualizer: Root represents the highest-priority request dispatched next. Swaps during sift-up and sift-down are highlighted with blue pulse animation.");
        infoLabel->setStyleSheet("color: #37474f; font-weight: bold; margin-bottom: 5px;");
        layout->addWidget(infoLabel);

        visualizerWidget = new HeapTreeWidget(&heap);
        visualizerScroll = new QScrollArea();
        visualizerScroll->setWidgetResizable(true);
        visualizerScroll->setWidget(visualizerWidget);
        layout->addWidget(visualizerScroll);

        return page;
    }

    // ------------------------------------------------------------------------
    // TAB 4: History
    // ------------------------------------------------------------------------
    QWidget* createTabHistory() {
        QWidget* page = new QWidget();
        QVBoxLayout* layout = new QVBoxLayout(page);
        layout->setContentsMargins(15, 15, 15, 15);

        QLabel* title = new QLabel("Incident Resolution Audit Log (Completed & Cancelled Requests)");
        title->setStyleSheet("font-size: 15px; font-weight: bold; color: #37474f;");
        layout->addWidget(title);

        historyTable = new QTableWidget();
        historyTable->setColumnCount(9);
        historyTable->setHorizontalHeaderLabels({
            "Outcome Status", "ID", "Severity", "Service", "Caller Name",
            "Location", "Received Time", "Resolved Time", "Description"
        });
        historyTable->horizontalHeader()->setSectionResizeMode(QHeaderView::ResizeToContents);
        historyTable->horizontalHeader()->setSectionResizeMode(8, QHeaderView::Stretch);
        historyTable->setSelectionBehavior(QAbstractItemView::SelectRows);
        historyTable->verticalHeader()->setVisible(false);
        layout->addWidget(historyTable);

        return page;
    }

    // ------------------------------------------------------------------------
    // TAB 5: Analysis & Complexity Table
    // ------------------------------------------------------------------------
    QWidget* createTabAnalysis() {
        QWidget* page = new QWidget();
        QVBoxLayout* layout = new QVBoxLayout(page);
        layout->setContentsMargins(20, 20, 20, 20);
        layout->setSpacing(15);

        QLabel* title = new QLabel("Data Structures & Algorithms Complexity Analysis");
        title->setStyleSheet("font-size: 16px; font-weight: bold; color: #0d47a1;");
        layout->addWidget(title);

        complexityTable = new QTableWidget();
        complexityTable->setColumnCount(5);
        complexityTable->setHorizontalHeaderLabels({
            "Heap Operation", "Best Time", "Average Time", "Worst Time", "Auxiliary Space"
        });
        complexityTable->horizontalHeader()->setSectionResizeMode(QHeaderView::Stretch);
        complexityTable->verticalHeader()->setVisible(false);

        struct OpComplexity {
            QString op;
            QString best;
            QString avg;
            QString worst;
            QString space;
        };

        std::vector<OpComplexity> rows = {
            {"Insert (sift-up)", "O(1)", "O(log n)", "O(log n)", "O(1)"},
            {"Extract-Max (Dispatch)", "O(1)", "O(log n)", "O(log n)", "O(1)"},
            {"Peek (view highest)", "O(1)", "O(1)", "O(1)", "O(1)"},
            {"Change Severity (re-heapify)", "O(1)", "O(log n)", "O(log n)", "O(1)"},
            {"Cancel/Remove by ID", "O(1)", "O(log n)", "O(log n)", "O(1)"},
            {"Build-Heap (Bottom-Up)", "O(n)", "O(n)", "O(n)", "O(1)"},
            {"Heap Sort (Priority View)", "O(n log n)", "O(n log n)", "O(n log n)", "O(n)"},
            {"Search (Linear scan)", "O(1)", "O(n)", "O(n)", "O(1)"}
        };

        complexityTable->setRowCount(static_cast<int>(rows.size()));
        for (int i = 0; i < static_cast<int>(rows.size()); ++i) {
            complexityTable->setItem(i, 0, new QTableWidgetItem(rows[i].op));
            complexityTable->setItem(i, 1, new QTableWidgetItem(rows[i].best));
            complexityTable->setItem(i, 2, new QTableWidgetItem(rows[i].avg));
            complexityTable->setItem(i, 3, new QTableWidgetItem(rows[i].worst));
            complexityTable->setItem(i, 4, new QTableWidgetItem(rows[i].space));

            for (int j = 0; j < 5; ++j) {
                complexityTable->item(i, j)->setTextAlignment(Qt::AlignCenter);
                if (j == 0) complexityTable->item(i, j)->setTextAlignment(Qt::AlignLeft | Qt::AlignVCenter);
            }
        }
        layout->addWidget(complexityTable);

        // Live Operation Metrics Box
        QWidget* metricsCard = new QWidget();
        metricsCard->setStyleSheet("background-color: #f1f8e9; border: 1px solid #c5e1a5; border-radius: 8px; padding: 12px;");
        QVBoxLayout* cardLayout = new QVBoxLayout(metricsCard);

        QLabel* cardTitle = new QLabel("Live Telemetry: Last Performed Heap Operation");
        cardTitle->setStyleSheet("font-weight: bold; color: #33691e; font-size: 14px;");
        cardLayout->addWidget(cardTitle);

        QHBoxLayout* statsRow = new QHBoxLayout();
        lblLastOperation = new QLabel("Operation: None");
        lblLastOperation->setStyleSheet("font-weight: bold; color: #1b5e20;");
        statsRow->addWidget(lblLastOperation);

        lblComparisons = new QLabel("Comparisons: 0");
        lblComparisons->setStyleSheet("font-weight: bold; color: #e65100;");
        statsRow->addWidget(lblComparisons);

        lblSwaps = new QLabel("Swaps: 0");
        lblSwaps->setStyleSheet("font-weight: bold; color: #b71c1c;");
        statsRow->addWidget(lblSwaps);

        lblCurrentHeapSize = new QLabel("Pending Nodes (n): 0");
        lblCurrentHeapSize->setStyleSheet("font-weight: bold; color: #0d47a1;");
        statsRow->addWidget(lblCurrentHeapSize);

        lblTreeHeight = new QLabel("Tree Height: 0");
        lblTreeHeight->setStyleSheet("font-weight: bold; color: #4a148c;");
        statsRow->addWidget(lblTreeHeight);

        cardLayout->addLayout(statsRow);
        layout->addWidget(metricsCard);

        return page;
    }

    // ------------------------------------------------------------------------
    // UI SETUP: Status Bar
    // ------------------------------------------------------------------------
    void setupStatusBar() {
        QStatusBar* bar = statusBar();
        bar->setStyleSheet("background-color: #eceff1; color: #263238; font-weight: 500;");
        bar->showMessage("Ready | Max-Heap Initialized");
    }

    // ------------------------------------------------------------------------
    // DATA & EVENT HANDLERS
    // ------------------------------------------------------------------------

    void onSubmitNewRequest() {
        QString name = editCallerName->text().trimmed();
        QString phone = editPhone->text().trimmed();
        QString location = editLocation->text().trimmed();
        int severity = comboSeverity->currentData().toInt();
        QString service = comboService->currentText();
        QString desc = editDescription->toPlainText().trimmed();

        // Validation
        if (name.isEmpty()) {
            QMessageBox::warning(this, "Validation Error", "Caller name cannot be empty.");
            editCallerName->setFocus();
            return;
        }

        static QRegularExpression phoneRegex("^[0-9]{10}$");
        if (!phoneRegex.match(phone).hasMatch()) {
            QMessageBox::warning(this, "Validation Error", "Phone number must be exactly 10 digits.");
            editPhone->setFocus();
            return;
        }

        if (location.isEmpty()) {
            QMessageBox::warning(this, "Validation Error", "Incident location cannot be empty.");
            editLocation->setFocus();
            return;
        }

        EmergencyRequest req;
        req.id = "REQ-" + std::to_string(nextSequenceNumber);
        req.callerName = name;
        req.phoneNumber = phone;
        req.location = location;
        req.serviceType = service;
        req.severity = severity;
        req.description = desc.isEmpty() ? "No description provided." : desc;
        req.sequenceNumber = nextSequenceNumber++;
        req.timestamp = QDateTime::currentDateTime().toString("yyyy-MM-dd hh:mm:ss");
        req.status = RequestStatus::Pending;

        heap.insert(req);

        // Clear Form
        editCallerName->clear();
        editPhone->clear();
        editLocation->clear();
        editDescription->clear();
        comboSeverity->setCurrentIndex(2);

        refreshAllViews();

        // Highlight in visualizer if swapped
        const auto& swaps = heap.getRecordedSwaps();
        if (!swaps.empty()) {
            visualizerWidget->triggerHighlight(swaps.back().first, swaps.back().second);
        }

        QMessageBox::information(this, "Request Queued",
                                 QString("Emergency request %1 has been successfully inserted into the Max-Heap!\nPriority Severity: Level %2.")
                                 .arg(QString::fromStdString(req.id)).arg(severity));
    }

    void onDispatchNext() {
        if (heap.empty()) {
            QMessageBox::warning(this, "Empty Queue", "No pending emergency requests available to dispatch.");
            return;
        }

        EmergencyRequest dispatched;
        if (heap.extractMax(dispatched)) {
            dispatched.status = RequestStatus::Completed;
            dispatched.resolutionTime = QDateTime::currentDateTime().toString("yyyy-MM-dd hh:mm:ss");
            history.push_back(dispatched);

            refreshAllViews();

            QMessageBox::information(this, "Unit Dispatched",
                                     QString("Dispatched Unit for Highest Priority Call:\n\n"
                                             "ID: %1\n"
                                             "Severity: %2\n"
                                             "Service: %3\n"
                                             "Caller: %4 (%5)\n"
                                             "Location: %6\n"
                                             "Timestamp: %7")
                                     .arg(QString::fromStdString(dispatched.id))
                                     .arg(EmergencyRequest::severityToString(dispatched.severity))
                                     .arg(dispatched.serviceType)
                                     .arg(dispatched.callerName)
                                     .arg(dispatched.phoneNumber)
                                     .arg(dispatched.location)
                                     .arg(dispatched.timestamp));
        }
    }

    void onChangeSeverityClicked() {
        int selectedRow = queueTable->currentRow();
        if (selectedRow < 0) {
            QMessageBox::information(this, "Select Request", "Please select a row in the priority queue table first.");
            return;
        }

        std::string reqId = queueTable->item(selectedRow, 1)->text().toStdString();
        const EmergencyRequest* req = heap.getById(reqId);
        if (!req) return;

        QStringList items;
        items << "1 - Low" << "2 - Moderate" << "3 - High" << "4 - Critical";

        bool ok;
        QString item = QInputDialog::getItem(this, "Change Severity",
                                             QString("Change severity for %1 (%2):").arg(QString::fromStdString(req->id)).arg(req->callerName),
                                             items, req->severity - 1, false, &ok);
        if (ok && !item.isEmpty()) {
            int newSeverity = item.left(1).toInt();
            heap.changeSeverity(reqId, newSeverity);
            refreshAllViews();

            const auto& swaps = heap.getRecordedSwaps();
            if (!swaps.empty()) {
                visualizerWidget->triggerHighlight(swaps.back().first, swaps.back().second);
            }
        }
    }

    void onCancelRequestClicked() {
        int selectedRow = queueTable->currentRow();
        if (selectedRow < 0) {
            QMessageBox::information(this, "Select Request", "Please select a row in the priority queue table first.");
            return;
        }

        std::string reqId = queueTable->item(selectedRow, 1)->text().toStdString();

        QMessageBox::StandardButton reply = QMessageBox::question(
            this, "Confirm Cancellation",
            QString("Are you sure you want to cancel request %1?").arg(QString::fromStdString(reqId)),
            QMessageBox::Yes | QMessageBox::No
        );

        if (reply == QMessageBox::Yes) {
            EmergencyRequest removed;
            if (heap.removeById(reqId, removed)) {
                removed.status = RequestStatus::Cancelled;
                removed.resolutionTime = QDateTime::currentDateTime().toString("yyyy-MM-dd hh:mm:ss");
                history.push_back(removed);
                refreshAllViews();
                QMessageBox::information(this, "Cancelled", QString("Request %1 cancelled and archived to history.").arg(QString::fromStdString(reqId)));
            }
        }
    }

    void onSearchChanged(const QString& query) {
        populateQueueTable(query);
    }

    void loadDemoData() {
        struct DemoItem {
            const char* name;
            const char* phone;
            const char* loc;
            const char* service;
            int severity;
            const char* desc;
        };

        std::vector<DemoItem> sample = {
            {"Ayesha Khan", "9876543201", "Mehdipatnam X Roads", "Ambulance", 4, "Cardiac arrest in public transit, breathing faint."},
            {"Srikanth Reddy", "9848012345", "HITEC City Phase 2", "Fire", 4, "Commercial building electrical fire, floor 4 trapped."},
            {"Mohd Irfan", "9123456789", "Rajendranagar Ring Road", "Police", 3, "Armed robbery reported at fuel station, suspects fled."},
            {"Lakshmi Priya", "9988776655", "Gachibowli Flyover", "Ambulance", 3, "High-speed two-wheeler crash, head trauma reported."},
            {"Venkat Rao", "9012345678", "Koti Market Lane", "Police", 2, "Public brawl with minor injuries, traffic blocked."},
            {"Ananya Sen", "9765432109", "Secunderabad Station", "Other", 1, "Elderly passenger lost luggage with vital medical docs."},
            {"David Williams", "9887766554", "Begumpet Airport Rd", "Ambulance", 4, "Mass casualty collision involving passenger bus."},
            {"Kavita Sharma", "9554433221", "Banjara Hills Rd #12", "Disaster Relief", 3, "Heavy rainfall caused retaining wall collapse."},
            {"Ramesh Babu", "9443322110", "Charminar Heritage Area", "Fire", 2, "Small transformer spark caught textile awning."},
            {"Fatima Begum", "9332211009", "Tolichowki Flyover", "Ambulance", 1, "Minor sprain after slip, requesting routine transport."}
        };

        for (const auto& item : sample) {
            EmergencyRequest req;
            req.id = "REQ-" + std::to_string(nextSequenceNumber);
            req.callerName = item.name;
            req.phoneNumber = item.phone;
            req.location = item.loc;
            req.serviceType = item.service;
            req.severity = item.severity;
            req.description = item.desc;
            req.sequenceNumber = nextSequenceNumber++;
            req.timestamp = QDateTime::currentDateTime().toString("yyyy-MM-dd hh:mm:ss");
            req.status = RequestStatus::Pending;
            heap.insert(req);
        }

        refreshAllViews();
        tabWidget->setCurrentIndex(1); // Jump to Queue table
        QMessageBox::information(this, "Demo Loaded", "Successfully loaded 10 emergency requests into the Max-Heap!");
    }

    void onSaveToFile() {
        QString fileName = QFileDialog::getSaveFileName(this, "Save Emergency Requests", "emergency_requests.csv", "CSV Files (*.csv);;Text Files (*.txt)");
        if (fileName.isEmpty()) return;

        QFile file(fileName);
        if (!file.open(QIODevice::WriteOnly | QIODevice::Text)) {
            QMessageBox::critical(this, "File Error", "Could not open file for writing: " + file.errorString());
            return;
        }

        QTextStream out(&file);
        // Header
        out << "ID,CallerName,Phone,Location,ServiceType,Severity,SequenceNumber,Timestamp,Status,Description\n";

        const auto& raw = heap.getRawHeap();
        for (const auto& r : raw) {
            out << QString::fromStdString(r.id) << ","
                << escapeCsv(r.callerName) << ","
                << r.phoneNumber << ","
                << escapeCsv(r.location) << ","
                << r.serviceType << ","
                << r.severity << ","
                << r.sequenceNumber << ","
                << r.timestamp << ","
                << static_cast<int>(r.status) << ","
                << escapeCsv(r.description) << "\n";
        }
        file.close();
        QMessageBox::information(this, "Saved", QString("Successfully saved %1 pending requests to %2.").arg(raw.size()).arg(fileName));
    }

    void onLoadFromFile() {
        QString fileName = QFileDialog::getOpenFileName(this, "Load Emergency Requests", "", "CSV Files (*.csv);;Text Files (*.txt);;All Files (*)");
        if (fileName.isEmpty()) return;

        QFile file(fileName);
        if (!file.open(QIODevice::ReadOnly | QIODevice::Text)) {
            QMessageBox::critical(this, "File Error", "Could not open file for reading: " + file.errorString());
            return;
        }

        QTextStream in(&file);
        QString headerLine = in.readLine(); // Skip CSV header

        std::vector<EmergencyRequest> loadedRequests;
        uint64_t maxSeq = nextSequenceNumber;

        int lineNum = 1;
        while (!in.atEnd()) {
            lineNum++;
            QString line = in.readLine().trimmed();
            if (line.isEmpty()) continue;

            QStringList parts = parseCsvLine(line);
            if (parts.size() < 10) {
                QMessageBox::warning(this, "Corrupt File", QString("Row %1 has invalid format. Aborting load.").arg(lineNum));
                return;
            }

            EmergencyRequest req;
            req.id = parts[0].toStdString();
            req.callerName = parts[1];
            req.phoneNumber = parts[2];
            req.location = parts[3];
            req.serviceType = parts[4];
            req.severity = parts[5].toInt();
            req.sequenceNumber = parts[6].toULongLong();
            req.timestamp = parts[7];
            req.status = static_cast<RequestStatus>(parts[8].toInt());
            req.description = parts[9];

            if (req.sequenceNumber >= maxSeq) {
                maxSeq = req.sequenceNumber + 1;
            }

            loadedRequests.push_back(req);
        }
        file.close();

        nextSequenceNumber = maxSeq;

        // O(n) Build Heap construction
        heap.buildHeap(loadedRequests);
        refreshAllViews();

        QMessageBox::information(this, "Load Complete",
                                 QString("Loaded %1 requests and constructed Max-Heap using O(n) Build-Heap algorithm.")
                                 .arg(loadedRequests.size()));
    }

    void showAboutDialog() {
        QMessageBox::about(this, "About Emergency Prioritizer",
                           "<h2>Emergency Service Request Prioritizer</h2>"
                           "<p><b>Institution:</b> Lords Institute of Engineering & Technology (Autonomous)</p>"
                           "<p><b>Department:</b> Computer Science & Engineering (AI & ML)</p>"
                           "<p><b>Course:</b> Data Structures & Algorithms (Assignment-III / Micro-Project #17)</p>"
                           "<hr/>"
                           "<p><b>Core Data Structure:</b> Hand-crafted Binary Max-Heap with O(1) hash index mapping.</p>"
                           "<p><b>Sorting Guarantee:</b> Strict Max-Heap priority by Severity (4 Critical down to 1 Low), "
                           "with deterministic FIFO tie-breaking based on arrival sequence numbers.</p>"
                           "<p><b>Performance:</b> O(1) peek, O(log n) insert/extract/update, and O(n) build-heap.</p>");
    }

    // ------------------------------------------------------------------------
    // VIEW REFRESH LOGIC
    // ------------------------------------------------------------------------
    void refreshAllViews() {
        populateQueueTable(searchBox ? searchBox->text() : "");
        populateHistoryTable();
        updateAnalysisTab();
        updateStatusBar();
        if (visualizerWidget) {
            visualizerWidget->update();
        }
    }

    void populateQueueTable(const QString& filter) {
        queueTable->setRowCount(0);

        // Retrieve sorted view without destroying the heap
        std::vector<EmergencyRequest> sorted = heap.getSortedRequests();

        int rank = 1;
        for (const auto& req : sorted) {
            // Filter match check
            if (!filter.isEmpty()) {
                QString q = filter.toLower();
                bool match = QString::fromStdString(req.id).toLower().contains(q) ||
                             req.callerName.toLower().contains(q) ||
                             req.location.toLower().contains(q) ||
                             req.serviceType.toLower().contains(q) ||
                             req.phoneNumber.contains(q);
                if (!match) continue;
            }

            int row = queueTable->rowCount();
            queueTable->insertRow(row);

            QTableWidgetItem* itemRank = new QTableWidgetItem(QString("#%1").arg(rank++));
            QTableWidgetItem* itemId = new QTableWidgetItem(QString::fromStdString(req.id));
            QTableWidgetItem* itemSev = new QTableWidgetItem(EmergencyRequest::severityToString(req.severity));
            QTableWidgetItem* itemSvc = new QTableWidgetItem(req.serviceType);
            QTableWidgetItem* itemName = new QTableWidgetItem(req.callerName);
            QTableWidgetItem* itemPhone = new QTableWidgetItem(req.phoneNumber);
            QTableWidgetItem* itemLoc = new QTableWidgetItem(req.location);
            QTableWidgetItem* itemSeq = new QTableWidgetItem(QString::number(req.sequenceNumber));
            QTableWidgetItem* itemTime = new QTableWidgetItem(req.timestamp);
            QTableWidgetItem* itemDesc = new QTableWidgetItem(req.description);

            // Color Coding based on Severity
            QColor rowColor;
            switch (req.severity) {
                case 4: rowColor = QColor(255, 205, 210); break; // Soft Red
                case 3: rowColor = QColor(255, 224, 178); break; // Soft Orange
                case 2: rowColor = QColor(255, 249, 196); break; // Soft Yellow
                case 1: default: rowColor = QColor(200, 230, 201); break; // Soft Green
            }

            std::vector<QTableWidgetItem*> items = {itemRank, itemId, itemSev, itemSvc, itemName, itemPhone, itemLoc, itemSeq, itemTime, itemDesc};
            for (auto* itm : items) {
                itm->setBackground(rowColor);
                itm->setForeground(Qt::black);
            }

            queueTable->setItem(row, 0, itemRank);
            queueTable->setItem(row, 1, itemId);
            queueTable->setItem(row, 2, itemSev);
            queueTable->setItem(row, 3, itemSvc);
            queueTable->setItem(row, 4, itemName);
            queueTable->setItem(row, 5, itemPhone);
            queueTable->setItem(row, 6, itemLoc);
            queueTable->setItem(row, 7, itemSeq);
            queueTable->setItem(row, 8, itemTime);
            queueTable->setItem(row, 9, itemDesc);
        }
    }

    void populateHistoryTable() {
        historyTable->setRowCount(0);
        for (int i = static_cast<int>(history.size()) - 1; i >= 0; --i) {
            const auto& req = history[i];
            int row = historyTable->rowCount();
            historyTable->insertRow(row);

            QTableWidgetItem* itemStatus = new QTableWidgetItem(EmergencyRequest::statusToString(req.status));
            if (req.status == RequestStatus::Completed) {
                itemStatus->setForeground(QColor(46, 125, 50));
            } else {
                itemStatus->setForeground(QColor(198, 40, 40));
            }

            historyTable->setItem(row, 0, itemStatus);
            historyTable->setItem(row, 1, new QTableWidgetItem(QString::fromStdString(req.id)));
            historyTable->setItem(row, 2, new QTableWidgetItem(EmergencyRequest::severityToString(req.severity)));
            historyTable->setItem(row, 3, new QTableWidgetItem(req.serviceType));
            historyTable->setItem(row, 4, new QTableWidgetItem(req.callerName));
            historyTable->setItem(row, 5, new QTableWidgetItem(req.location));
            historyTable->setItem(row, 6, new QTableWidgetItem(req.timestamp));
            historyTable->setItem(row, 7, new QTableWidgetItem(req.resolutionTime));
            historyTable->setItem(row, 8, new QTableWidgetItem(req.description));
        }
    }

    void updateAnalysisTab() {
        lblLastOperation->setText("Operation: " + QString::fromStdString(heap.getLastOperationName()));
        lblComparisons->setText("Comparisons: " + QString::number(heap.getLastComparisons()));
        lblSwaps->setText("Swaps: " + QString::number(heap.getLastSwaps()));
        lblCurrentHeapSize->setText("Pending Nodes (n): " + QString::number(heap.size()));

        int h = heap.empty() ? 0 : static_cast<int>(std::floor(std::log2(heap.size()))) + 1;
        lblTreeHeight->setText("Tree Height: " + QString::number(h));
    }

    void updateStatusBar() {
        if (heap.empty()) {
            statusBar()->showMessage("Pending Requests: 0 | Priority Queue Empty | System Idle");
            btnDispatchNext->setEnabled(false);
            btnChangeSeverity->setEnabled(false);
            btnCancelRequest->setEnabled(false);
        } else {
            EmergencyRequest top;
            heap.peek(top);
            QString statusMsg = QString("Pending Requests: %1 | 🚨 Next Priority: [%2] %3 - %4 (Caller: %5, Location: %6)")
                                .arg(heap.size())
                                .arg(QString::fromStdString(top.id))
                                .arg(EmergencyRequest::severityToString(top.severity))
                                .arg(top.serviceType)
                                .arg(top.callerName)
                                .arg(top.location);
            statusBar()->showMessage(statusMsg);
            btnDispatchNext->setEnabled(true);
            btnChangeSeverity->setEnabled(true);
            btnCancelRequest->setEnabled(true);
        }
    }

    // CSV Helper functions
    static QString escapeCsv(const QString& str) {
        QString s = str;
        s.replace("\"", "\"\"");
        return "\"" + s + "\"";
    }

    static QStringList parseCsvLine(const QString& line) {
        QStringList result;
        QString cur;
        bool inQuotes = false;
        for (int i = 0; i < line.size(); ++i) {
            QChar c = line[i];
            if (c == '\"') {
                if (inQuotes && i + 1 < line.size() && line[i + 1] == '\"') {
                    cur += '\"';
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.append(cur);
                cur.clear();
            } else {
                cur += c;
            }
        }
        result.append(cur);
        return result;
    }
};

// ============================================================================
// 5. APPLICATION ENTRY POINT
// ============================================================================
int main(int argc, char* argv[]) {
    // Enable High DPI scaling
#if QT_VERSION < QT_VERSION_CHECK(6, 0, 0)
    QApplication::setAttribute(Qt::AA_EnableHighDpiScaling);
    QApplication::setAttribute(Qt::AA_UseHighDpiPixmaps);
#endif

    QApplication app(argc, argv);
    app.setApplicationName("EmergencyServicePrioritizer");
    app.setOrganizationName("LordsLIET_AIML");

    MainWindow window;
    window.show();

    return app.exec();
}

#include "main.moc"
