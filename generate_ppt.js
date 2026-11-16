const pptxgen = require('pptxgenjs');
const fs = require('fs');
const path = require('path');

async function createPresentation() {
    const pres = new pptxgen();
    pres.layout = 'LAYOUT_16x9'; // 10 x 5.625 inches
    pres.author = 'Mohammed Irfan';
    pres.company = 'Lords Institute of Engineering & Technology (UGC Autonomous)';
    pres.subject = 'Emergency Service Request Prioritizer - DSA Assignment-III';
    pres.title = 'Emergency Service Request Prioritizer Presentation';

    // Theme Palette: Luxury Tactical Dark
    const BG_DARK = '0B0C10';        // Deep Obsidian
    const CARD_BG = '14161F';        // Dark Surface Card
    const CARD_BORDER = '232738';    // Border Stroke
    const ACCENT_RED = 'E53935';     // Emergency Red
    const ACCENT_GOLD = 'FFB300';    // Priority Gold
    const ACCENT_BLUE = '42A5F5';    // Tech Blue
    const ACCENT_GREEN = '00E676';   // Status Green
    const TEXT_WHITE = 'FFFFFF';     // Primary Text
    const TEXT_MUTED = '9EA3B8';     // Secondary Text
    const TEXT_SUBTLE = '60667D';    // Muted Gray

    // Helper: Add consistent slide background & header
    function setupSlide(title, subtitle, slideNum) {
        const slide = pres.addSlide();
        slide.background = { color: BG_DARK };

        // Top Header Accent Line
        slide.addShape(pres.ShapeType.rect, {
            x: 0.5, y: 0.4, w: 0.15, h: 0.55,
            fill: { color: ACCENT_RED },
            line: { color: ACCENT_RED }
        });

        // Title and Subtitle
        slide.addText(title, {
            x: 0.8, y: 0.35, w: 8.0, h: 0.4,
            fontSize: 20, fontFace: 'Calibri', bold: true, color: TEXT_WHITE
        });
        if (subtitle) {
            slide.addText(subtitle, {
                x: 0.8, y: 0.72, w: 8.0, h: 0.25,
                fontSize: 10, fontFace: 'Calibri', color: ACCENT_BLUE
            });
        }

        // Institutional Header Top-Right
        slide.addText('LIET (UGC Autonomous) • Dept. of CSE-AIML', {
            x: 5.5, y: 0.35, w: 4.0, h: 0.3,
            fontSize: 9, fontFace: 'Calibri', color: TEXT_MUTED, align: 'right'
        });

        // Slide Number Pill Bottom-Right
        slide.addText(`Slide ${slideNum} / 12`, {
            x: 8.6, y: 5.25, w: 1.0, h: 0.25,
            fontSize: 9, fontFace: 'Calibri', color: TEXT_SUBTLE, align: 'right'
        });

        // Bottom Institutional Footer
        slide.addText('Data Structures & Algorithms Laboratory (Assignment-III) | Project #17: Emergency Service Request Prioritizer', {
            x: 0.5, y: 5.25, w: 7.5, h: 0.25,
            fontSize: 8.5, fontFace: 'Calibri', color: TEXT_SUBTLE
        });

        return slide;
    }

    // ==========================================
    // SLIDE 1: Title Slide
    // ==========================================
    {
        const slide = pres.addSlide();
        slide.background = { color: BG_DARK };

        // Banner Card
        slide.addShape(pres.ShapeType.roundRect, {
            x: 0.8, y: 0.6, w: 8.4, h: 4.4,
            fill: { color: CARD_BG },
            line: { color: CARD_BORDER, width: 1.5 },
            rectRadius: 0.15
        });

        // Red Accent Glow Bar
        slide.addShape(pres.ShapeType.rect, {
            x: 1.2, y: 1.0, w: 0.12, h: 1.2,
            fill: { color: ACCENT_RED },
            line: { color: ACCENT_RED }
        });

        slide.addText('LORDS INSTITUTE OF ENGINEERING & TECHNOLOGY (UGC AUTONOMOUS)', {
            x: 1.5, y: 0.95, w: 7.2, h: 0.3,
            fontSize: 10, fontFace: 'Calibri', bold: true, color: ACCENT_GOLD, charSpacing: 1
        });
        slide.addText('Approved by AICTE | Affiliated to Osmania University | Department of CSE-AIML', {
            x: 1.5, y: 1.22, w: 7.2, h: 0.25,
            fontSize: 9, fontFace: 'Calibri', color: TEXT_MUTED
        });

        slide.addText('EMERGENCY SERVICE REQUEST PRIORITIZER', {
            x: 1.5, y: 1.55, w: 7.2, h: 0.7,
            fontSize: 24, fontFace: 'Calibri', bold: true, color: TEXT_WHITE
        });
        slide.addText('Real-Time Triage Engine Driven by a Hand-Crafted Binary Max-Heap Data Structure', {
            x: 1.5, y: 2.25, w: 7.2, h: 0.3,
            fontSize: 12, fontFace: 'Calibri', italic: true, color: ACCENT_BLUE
        });

        // Key Metadata Table / Details
        slide.addShape(pres.ShapeType.roundRect, {
            x: 1.2, y: 2.7, w: 7.6, h: 1.9,
            fill: { color: '0E1017' },
            line: { color: '1C2030', width: 1 },
            rectRadius: 0.1
        });

        const details = [
            [{ text: 'Project Number:', options: { bold: true, color: ACCENT_GOLD } }, { text: 'Project #17 (Data Structures Assignment-III)' }],
            [{ text: 'Student Name:', options: { bold: true, color: TEXT_WHITE } }, { text: 'Mohammed Irfan & Team' }],
            [{ text: 'Department:', options: { bold: true, color: TEXT_WHITE } }, { text: 'Computer Science & Engineering (AI & ML)' }],
            [{ text: 'Implementation Language:', options: { bold: true, color: ACCENT_GREEN } }, { text: 'Kotlin (Jetpack Compose) / C++ Core DSA Model' }],
            [{ text: 'Academic Year:', options: { bold: true, color: TEXT_WHITE } }, { text: '2026 - 2027 | Supervised by Course In-Charge' }]
        ];

        slide.addTable(details, {
            x: 1.4, y: 2.8, w: 7.2, h: 1.7,
            fontSize: 10.5, fontFace: 'Calibri',
            color: TEXT_MUTED,
            border: { pt: 0, color: '000000' }
        });
    }

    // ==========================================
    // SLIDE 2: Problem Statement
    // ==========================================
    {
        const slide = setupSlide('Problem Statement', 'Critical Challenges in Contemporary Emergency Dispatch Systems', 2);

        // 4 Key Question Cards
        const cards = [
            {
                title: 'Real-World Problem Addressed',
                icon: '🚨',
                desc: 'In conventional municipal call centers, requests are queued First-In-First-Out (FIFO). Life-critical calls (cardiac arrest, building fires) suffer life-threatening delays behind non-urgent utility calls (water tap leaks, low-voltage power trips).',
                accent: ACCENT_RED
            },
            {
                title: 'Who Faces This Critical Issue?',
                icon: '👥',
                desc: 'Emergency dispatchers (108 / 112 / 911 centers), hospital triage coordinators, frontline paramedics, and citizens experiencing acute life-threatening trauma whose survival depends on sub-minute dispatch.',
                accent: ACCENT_GOLD
            },
            {
                title: 'Limitations of Existing Approaches',
                icon: '⚠️',
                desc: '• Unsorted Lists: Finding the most critical emergency requires an O(n) linear scan across all active calls.\n• Sorted Lists: Inserting each new arrival incurs O(n) element shifts.\n• FIFO Queues: Zero triage awareness, causing severe priority inversion.',
                accent: ACCENT_BLUE
            },
            {
                title: 'Why an Application is Essential',
                icon: '💻',
                desc: 'A computerized Computer-Aided Dispatch (CAD) application backed by a logarithmic priority queue guarantees sub-second triage, automated severity detection, duplicate call filtering, and transparent visual auditability.',
                accent: ACCENT_GREEN
            }
        ];

        cards.forEach((c, idx) => {
            const x = 0.6 + (idx % 2) * 4.5;
            const y = 1.2 + Math.floor(idx / 2) * 1.9;

            slide.addShape(pres.ShapeType.roundRect, {
                x: x, y: y, w: 4.3, h: 1.75,
                fill: { color: CARD_BG },
                line: { color: c.accent, width: 1.2 },
                rectRadius: 0.1
            });

            slide.addText(`${c.icon}  ${c.title}`, {
                x: x + 0.2, y: y + 0.15, w: 3.9, h: 0.3,
                fontSize: 11.5, fontFace: 'Calibri', bold: true, color: c.accent
            });

            slide.addText(c.desc, {
                x: x + 0.2, y: y + 0.5, w: 3.9, h: 1.15,
                fontSize: 9.5, fontFace: 'Calibri', color: TEXT_WHITE, lineSpacing: 13
            });
        });
    }

    // ==========================================
    // SLIDE 3: Objectives & Scope
    // ==========================================
    {
        const slide = setupSlide('Objectives and Scope', 'Scope Boundaries, Functional Targets & Intended User Base', 3);

        // Left Column: Objectives & Included Features
        slide.addShape(pres.ShapeType.roundRect, {
            x: 0.6, y: 1.2, w: 4.3, h: 3.8,
            fill: { color: CARD_BG },
            line: { color: ACCENT_GREEN, width: 1.2 },
            rectRadius: 0.1
        });
        slide.addText('🎯 Project Objectives & Included Scope', {
            x: 0.8, y: 1.35, w: 3.9, h: 0.3,
            fontSize: 12, fontFace: 'Calibri', bold: true, color: ACCENT_GREEN
        });
        slide.addText([
            { text: '• Primary Objective: ', options: { bold: true, color: TEXT_WHITE } },
            { text: 'Build a production-grade dispatch platform placing high-severity emergencies ahead of routine calls using a hand-crafted Binary Max-Heap.\n\n', options: { color: TEXT_MUTED } },
            { text: '• Dual-Key Ordering: ', options: { bold: true, color: TEXT_WHITE } },
            { text: 'Priority based on Severity (4 Critical > 3 High > 2 Moderate > 1 Low), tie-broken by arrival sequence number (FIFO).\n\n', options: { color: TEXT_MUTED } },
            { text: '• Included Core Features: \n', options: { bold: true, color: TEXT_WHITE } },
            { text: '  1. User Registration & GPS coordinate intake\n  2. Automatic keyword-based priority classification\n  3. Hospital-grade ambulance confirmation screening\n  4. Real-time elapsed waiting time tracker with SLA\n  5. Interactive binary tree heap visualizer on Canvas\n  6. Personnel dispatch assignment & audit notifications\n  7. CSV export/import persistence without cloud dependency', options: { color: TEXT_MUTED } }
        ], {
            x: 0.8, y: 1.7, w: 3.9, h: 3.1,
            fontSize: 9, fontFace: 'Calibri', lineSpacing: 12
        });

        // Right Column: Excluded Scope & Target Audience
        slide.addShape(pres.ShapeType.roundRect, {
            x: 5.1, y: 1.2, w: 4.3, h: 1.8,
            fill: { color: CARD_BG },
            line: { color: ACCENT_RED, width: 1.2 },
            rectRadius: 0.1
        });
        slide.addText('🚫 Features Excluded (Deliberate Scope Limits)', {
            x: 5.3, y: 1.35, w: 3.9, h: 0.3,
            fontSize: 11.5, fontFace: 'Calibri', bold: true, color: ACCENT_RED
        });
        slide.addText([
            { text: '• Physical GPS Vehicle Telematics: ', options: { bold: true, color: TEXT_WHITE } },
            { text: 'Live vehicle tracker simulated via static location nodes rather than cellular OBD-II sensors.\n', options: { color: TEXT_MUTED } },
            { text: '• GSM Telephony Hardware: ', options: { bold: true, color: TEXT_WHITE } },
            { text: 'Zero external cellular SMS modem required; audit history logged in-app.\n', options: { color: TEXT_MUTED } },
            { text: '• Third-Party Queues: ', options: { bold: true, color: TEXT_WHITE } },
            { text: 'Strictly prohibited use of std::priority_queue or java.util.PriorityQueue.', options: { color: TEXT_MUTED } }
        ], {
            x: 5.3, y: 1.7, w: 3.9, h: 1.2,
            fontSize: 9, fontFace: 'Calibri', lineSpacing: 12
        });

        slide.addShape(pres.ShapeType.roundRect, {
            x: 5.1, y: 3.15, w: 4.3, h: 1.85,
            fill: { color: CARD_BG },
            line: { color: ACCENT_BLUE, width: 1.2 },
            rectRadius: 0.1
        });
        slide.addText('👥 Intended Users & Expected Outcomes', {
            x: 5.3, y: 3.3, w: 3.9, h: 0.3,
            fontSize: 11.5, fontFace: 'Calibri', bold: true, color: ACCENT_BLUE
        });
        slide.addText([
            { text: '• Primary Users: ', options: { bold: true, color: TEXT_WHITE } },
            { text: 'Emergency 108/112 dispatchers, police and fire command center officers, hospital ER staff.\n\n', options: { color: TEXT_MUTED } },
            { text: '• Measurable Outcome: ', options: { bold: true, color: TEXT_WHITE } },
            { text: 'Guaranteed sub-second priority placement; zero starvation of critical patients; O(log n) dynamic priority escalation and cancellation.', options: { color: TEXT_MUTED } }
        ], {
            x: 5.3, y: 3.65, w: 3.9, h: 1.25,
            fontSize: 9.5, fontFace: 'Calibri', lineSpacing: 13
        });
    }

    // ==========================================
    // SLIDE 4: Data Structure & Algorithm Selection
    // ==========================================
    {
        const slide = setupSlide('Data Structure & Algorithm Selection', 'Algorithmic Rationale, Contiguous Memory Model & Complexity Proof', 4);

        // 3 Cards Layout
        const dsItems = [
            {
                title: '1. Hand-Written Binary Max-Heap',
                role: 'Primary Priority Queue Structure',
                details: '• Underlying Storage: Contiguous dynamic array (ArrayList / Vector) with 0-based indexing.\n• Node Arithmetic: Parent(i) = (i-1)/2, Left(i) = 2i+1, Right(i) = 2i+2.\n• Invariant: Parent priority >= Child priority across all internal nodes.\n• Why Max-Heap? Provides O(log n) worst-case insertion and extraction, outperforming unsorted arrays O(n extract) and sorted arrays O(n insert). Contiguous memory guarantees zero cache thrashing.',
                color: ACCENT_RED
            },
            {
                title: '2. Auxiliary Hash Map (ID -> Index)',
                role: 'O(1) Direct Node Locator',
                details: '• Standard Problem: Locating an arbitrary request in a heap takes O(n) linear search.\n• Our Solution: Auxiliary HashMap<String, Int> tracking exact array index of every request ID.\n• Synchronized Swaps: When two nodes swap during sift-up/down, map entries update in O(1).\n• Impact: Enables dynamic changeSeverity() and removeById() in O(log n) rather than O(n).',
                color: ACCENT_GOLD
            },
            {
                title: '3. Floyd’s Bottom-Up Build-Heap',
                role: 'O(n) Linear Initialization',
                details: '• Algorithm: Populates array linearly, then invokes siftDown(i) from (n/2)-1 down to index 0.\n• Mathematical Proof: Leaves (height 0) require 0 swaps; sum of heights across tree converges:\n  Sum(h=0 to log n) [n / 2^(h+1)] * O(h) = O(n).\n• Application: Instant bulk loading of 10 demo competition scenarios and CSV file imports.',
                color: ACCENT_BLUE
            }
        ];

        dsItems.forEach((item, idx) => {
            const x = 0.6 + idx * 3.05;
            slide.addShape(pres.ShapeType.roundRect, {
                x: x, y: 1.2, w: 2.95, h: 3.8,
                fill: { color: CARD_BG },
                line: { color: item.color, width: 1.2 },
                rectRadius: 0.1
            });

            slide.addText(item.title, {
                x: x + 0.15, y: 1.35, w: 2.65, h: 0.35,
                fontSize: 11, fontFace: 'Calibri', bold: true, color: item.color
            });
            slide.addText(item.role, {
                x: x + 0.15, y: 1.7, w: 2.65, h: 0.25,
                fontSize: 9, fontFace: 'Calibri', italic: true, color: TEXT_MUTED
            });
            slide.addText(item.details, {
                x: x + 0.15, y: 2.05, w: 2.65, h: 2.8,
                fontSize: 8.5, fontFace: 'Calibri', color: TEXT_WHITE, lineSpacing: 12
            });
        });
    }

    // ==========================================
    // SLIDE 5: System Design & Information Flow
    // ==========================================
    {
        const slide = setupSlide('System Design & Architecture', 'Module Architecture, Information Flow & Data Organization', 5);

        // Architecture Pipeline Boxes
        const steps = [
            { num: 'STEP 1', name: 'User Registration & Intake', desc: 'Caller name, 10-digit mobile, location & GPS captured. Auto-priority keyword scan.', color: ACCENT_BLUE },
            { num: 'STEP 2', name: 'Ambulance Screening Flow', desc: 'Clinical symptom checklist. Critical symptoms escalate to S4; non-critical flagged for review.', color: ACCENT_GOLD },
            { num: 'STEP 3', name: 'Binary Max-Heap Engine', desc: 'Sift-up insertion in O(log n). Dual-key comparator (Severity + FIFO arrival tie-breaker).', color: ACCENT_RED },
            { num: 'STEP 4', name: 'CAD Queue & Dispatch', desc: 'Extract-max in O(log n). Personnel assigned; live waiting time and overdue SLA tracking.', color: ACCENT_GREEN },
            { num: 'STEP 5', name: 'History & CSV Engine', desc: 'Resolved requests archived to history queue. Plain-text CSV RFC-4180 export & import.', color: 'BA68C8' }
        ];

        steps.forEach((s, idx) => {
            const x = 0.6 + idx * 1.8;
            slide.addShape(pres.ShapeType.roundRect, {
                x: x, y: 1.2, w: 1.7, h: 2.3,
                fill: { color: CARD_BG },
                line: { color: s.color, width: 1.2 },
                rectRadius: 0.1
            });

            slide.addText(s.num, {
                x: x + 0.1, y: 1.35, w: 1.5, h: 0.25,
                fontSize: 9, fontFace: 'Calibri', bold: true, color: s.color, align: 'center'
            });
            slide.addText(s.name, {
                x: x + 0.1, y: 1.6, w: 1.5, h: 0.45,
                fontSize: 10, fontFace: 'Calibri', bold: true, color: TEXT_WHITE, align: 'center'
            });
            slide.addText(s.desc, {
                x: x + 0.1, y: 2.1, w: 1.5, h: 1.25,
                fontSize: 8, fontFace: 'Calibri', color: TEXT_MUTED, align: 'center'
            });

            // Arrow
            if (idx < 4) {
                slide.addText('➔', {
                    x: x + 1.65, y: 2.1, w: 0.2, h: 0.3,
                    fontSize: 12, fontFace: 'Calibri', bold: true, color: ACCENT_GOLD
                });
            }
        });

        // Bottom Module Diagram Box
        slide.addShape(pres.ShapeType.roundRect, {
            x: 0.6, y: 3.7, w: 8.8, h: 1.35,
            fill: { color: '0E1017' },
            line: { color: CARD_BORDER, width: 1 },
            rectRadius: 0.1
        });

        slide.addText('CORE DATA ORGANIZATION & REPOSITORIES', {
            x: 0.8, y: 3.85, w: 8.4, h: 0.25,
            fontSize: 10, fontFace: 'Calibri', bold: true, color: ACCENT_GOLD
        });
        slide.addText('• EmergencyMaxHeap (Contiguous Vector): Stores active pending calls in order of severity and arrival.\n• Auxiliary Map (HashMap<String, Int>): Maps each Request ID directly to its current heap array index for O(1) lookups.\n• History Audit Queue (ArrayList): Maintains chronological log of resolved and cancelled emergencies with timestamps.\n• Persistence Engine: Plain text CSV reader/writer ensuring offline durability and disaster recovery without external databases.', {
            x: 0.8, y: 4.15, w: 8.4, h: 0.8,
            fontSize: 8.5, fontFace: 'Calibri', color: TEXT_MUTED, lineSpacing: 12
        });
    }

    // ==========================================
    // SLIDE 6: Hardware & Software Requirements
    // ==========================================
    {
        const slide = setupSlide('Hardware & Software Requirements', 'System Environment, Compiler Toolchain & Operational Constraints', 6);

        // Hardware Specs Table
        const hwData = [
            [{ text: 'Hardware Component', options: { bold: true, color: ACCENT_GOLD } }, { text: 'Minimum Specification', options: { bold: true, color: ACCENT_GOLD } }, { text: 'Recommended Production Spec', options: { bold: true, color: ACCENT_GOLD } }],
            [{ text: 'Processor (CPU)' }, { text: 'Dual-Core 1.8 GHz (Intel i3 / ARMv7)' }, { text: 'Octa-Core 2.4 GHz (Intel i5 / ARM64)' }],
            [{ text: 'System Memory (RAM)' }, { text: '2 GB RAM' }, { text: '4 GB to 8 GB RAM' }],
            [{ text: 'Storage Footprint' }, { text: '100 MB free storage' }, { text: '500 MB SSD' }],
            [{ text: 'Network Requirement' }, { text: '0 Mbps (100% Fully Offline)' }, { text: 'Zero Internet Needed at Runtime' }]
        ];

        slide.addTable(hwData, {
            x: 0.6, y: 1.25, w: 8.8, h: 1.6,
            fontSize: 9, fontFace: 'Calibri',
            color: TEXT_WHITE,
            fill: { color: CARD_BG },
            border: { pt: 0.5, color: CARD_BORDER }
        });

        // Software Specs Table
        const swData = [
            [{ text: 'Software Layer', options: { bold: true, color: ACCENT_BLUE } }, { text: 'Tool / Framework Used', options: { bold: true, color: ACCENT_BLUE } }, { text: 'Version & Configuration', options: { bold: true, color: ACCENT_BLUE } }],
            [{ text: 'Operating System' }, { text: 'Android OS / Linux / Windows 10/11' }, { text: 'Android 12+ (API 34) / GCC MinGW 11+' }],
            [{ text: 'Language & Toolchain' }, { text: 'Kotlin / JVM 17 & C++17 Core' }, { text: 'Android Gradle Plugin & Modern Kotlin Compiler' }],
            [{ text: 'GUI & Graphics API' }, { text: 'Jetpack Compose & Android Canvas 2D' }, { text: 'Material Design 3 (M3) Dark Tactical Theme' }],
            [{ text: 'Persistence Format' }, { text: 'Plain Text / Standard RFC-4180 CSV' }, { text: 'Fully autonomous local file system storage' }]
        ];

        slide.addTable(swData, {
            x: 0.6, y: 3.15, w: 8.8, h: 1.8,
            fontSize: 9, fontFace: 'Calibri',
            color: TEXT_WHITE,
            fill: { color: CARD_BG },
            border: { pt: 0.5, color: CARD_BORDER }
        });
    }

    // ==========================================
    // SLIDE 7: Application Features & Implementation
    // ==========================================
    {
        const slide = setupSlide('Application Features & Implementation', 'Implementation of 9 Advanced Dispatch & Clinical Triage Capabilities', 7);

        const features = [
            { id: 'F-1', name: 'Auto Priority Detection', desc: 'Keyword NLP parser analyzes emergency notes (e.g. cardiac, fire, leak) to set initial severity.' },
            { id: 'F-2', name: 'Critical Emergency Alert', desc: 'Flashing audio/visual banners activate when Level 4 incidents arrive in the priority queue.' },
            { id: 'F-3', name: 'Waiting Time Tracker', desc: 'Real-time elapsed clock calculating response delay; visual SLA warnings for calls over 3 mins.' },
            { id: 'F-4', name: 'Location & GPS Intake', desc: 'Direct capture of street addresses with latitude/longitude coordinate estimation.' },
            { id: 'F-5', name: 'Duplicate Detection', desc: 'Identifies repeated incoming calls from the same phone number or zone to avoid fleet waste.' },
            { id: 'F-6', name: 'Executive CAD Dashboard', desc: 'Live counters showing Total Calls, Pending Queue, Critical Incidents, and Completed Runs.' },
            { id: 'F-7', name: 'Request Cancellation', desc: 'Removes tickets no longer required, re-heapifies in O(log n), and archives resolution reasons.' },
            { id: 'F-8', name: 'Personnel Assignment', desc: 'Assigns units (Ambulance Unit 01, Fire Squad Alpha, Rapid Response Police) to tickets.' },
            { id: 'F-9', name: 'Clinical Screening Flow', desc: 'Zero automated denials: 7-point symptom triage auto-escalates to S4 or queues for review.' }
        ];

        features.forEach((f, idx) => {
            const x = 0.6 + (idx % 3) * 3.0;
            const y = 1.25 + Math.floor(idx / 3) * 1.2;

            slide.addShape(pres.ShapeType.roundRect, {
                x: x, y: y, w: 2.85, h: 1.1,
                fill: { color: CARD_BG },
                line: { color: CARD_BORDER, width: 1 },
                rectRadius: 0.08
            });

            slide.addText(`${f.id}: ${f.name}`, {
                x: x + 0.1, y: y + 0.1, w: 2.65, h: 0.25,
                fontSize: 10, fontFace: 'Calibri', bold: true, color: ACCENT_GOLD
            });
            slide.addText(f.desc, {
                x: x + 0.1, y: y + 0.35, w: 2.65, h: 0.7,
                fontSize: 8, fontFace: 'Calibri', color: TEXT_MUTED, lineSpacing: 11
            });
        });
    }

    // ==========================================
    // SLIDE 8: Testing & Verification Matrix
    // ==========================================
    {
        const slide = setupSlide('Testing and Results', 'Automated Verification Matrix Covering Normal, Boundary & Edge Cases', 8);

        const testData = [
            [{ text: 'ID', options: { bold: true, color: ACCENT_GOLD } }, { text: 'Test Scenario', options: { bold: true, color: ACCENT_GOLD } }, { text: 'Input Data', options: { bold: true, color: ACCENT_GOLD } }, { text: 'Expected Outcome', options: { bold: true, color: ACCENT_GOLD } }, { text: 'Actual Result', options: { bold: true, color: ACCENT_GOLD } }, { text: 'Status', options: { bold: true, color: ACCENT_GREEN } }],
            [{ text: 'TC-01' }, { text: 'Empty Queue' }, { text: 'Extract-Max on n=0' }, { text: 'Graceful warning message; zero crash' }, { text: 'Warning displayed; safe return' }, { text: 'PASS' }],
            [{ text: 'TC-02' }, { text: 'Priority Ordering' }, { text: 'Insert S1 then S4' }, { text: 'S4 sifts up to index 0 (Root)' }, { text: 'S4 placed at root' }, { text: 'PASS' }],
            [{ text: 'TC-03' }, { text: 'FIFO Tie-Breaker' }, { text: 'Two S4 calls (Seq 1, 2)' }, { text: 'Seq 1 dispatched before Seq 2' }, { text: 'Exact FIFO ordering' }, { text: 'PASS' }],
            [{ text: 'TC-04' }, { text: 'Dynamic Escalation' }, { text: 'Update call from S2 to S4' }, { text: 'Sifts up to root in O(log n)' }, { text: 'Promoted to root instantly' }, { text: 'PASS' }],
            [{ text: 'TC-05' }, { text: 'Root Cancellation' }, { text: 'removeById(Root ID)' }, { text: 'Last node placed at root & sifted down' }, { text: 'Root removed; heap valid' }, { text: 'PASS' }],
            [{ text: 'TC-06' }, { text: 'Duplicate Filtering' }, { text: 'Identical phone & zone' }, { text: 'Warning modal shown; blocked' }, { text: 'Duplicate flagged' }, { text: 'PASS' }],
            [{ text: 'TC-07' }, { text: 'Ambulance Screen' }, { text: 'Chest pain selected' }, { text: 'Immediate auto-escalation to S4' }, { text: 'Priority elevated to S4' }, { text: 'PASS' }],
            [{ text: 'TC-08' }, { text: 'Stress Benchmark' }, { text: '200 Inserts + 200 Extracts' }, { text: 'Completed in < 20 ms; heap valid' }, { text: 'Done in 9.4 ms (100% Valid)' }, { text: 'PASS' }]
        ];

        slide.addTable(testData, {
            x: 0.6, y: 1.25, w: 8.8, h: 3.7,
            fontSize: 8.5, fontFace: 'Calibri',
            color: TEXT_WHITE,
            fill: { color: CARD_BG },
            border: { pt: 0.5, color: CARD_BORDER }
        });
    }

    // ==========================================
    // SLIDE 9: Performance Analysis & Complexity Matrix
    // ==========================================
    {
        const slide = setupSlide('Performance Analysis', 'Time and Space Complexity with Theoretical & Empirical Justifications', 9);

        const perfData = [
            [{ text: 'Operation', options: { bold: true, color: ACCENT_GOLD } }, { text: 'Best Case', options: { bold: true, color: ACCENT_GREEN } }, { text: 'Average Case', options: { bold: true, color: ACCENT_BLUE } }, { text: 'Worst Case', options: { bold: true, color: ACCENT_RED } }, { text: 'Space', options: { bold: true, color: TEXT_WHITE } }, { text: 'Mathematical Justification', options: { bold: true, color: ACCENT_GOLD } }],
            [{ text: 'Insert (sift-up)' }, { text: 'O(1)' }, { text: 'O(log n)' }, { text: 'O(log n)' }, { text: 'O(1)' }, { text: 'Added at array end; sifts up tree height <= floor(log2 n)' }],
            [{ text: 'Extract-Max (Dispatch)' }, { text: 'O(1)' }, { text: 'O(log n)' }, { text: 'O(log n)' }, { text: 'O(1)' }, { text: 'Root popped; last leaf placed at root and sifted down' }],
            [{ text: 'Peek Root' }, { text: 'O(1)' }, { text: 'O(1)' }, { text: 'O(1)' }, { text: 'O(1)' }, { text: 'Direct index 0 memory access' }],
            [{ text: 'Change Severity' }, { text: 'O(1)' }, { text: 'O(log n)' }, { text: 'O(log n)' }, { text: 'O(1)' }, { text: 'Hash map finds index in O(1); sift-up/down takes O(log n)' }],
            [{ text: 'Cancel / Remove by ID' }, { text: 'O(1)' }, { text: 'O(log n)' }, { text: 'O(log n)' }, { text: 'O(1)' }, { text: 'O(1) hash lookup; swap with last element & re-heapify' }],
            [{ text: 'Build-Heap (Floyd)' }, { text: 'O(n)' }, { text: 'O(n)' }, { text: 'O(n)' }, { text: 'O(1)' }, { text: 'Bottom-up sift-down from n/2 - 1 down to 0; sum converges to O(n)' }],
            [{ text: 'Heap Sort View' }, { text: 'O(n log n)' }, { text: 'O(n log n)' }, { text: 'O(n log n)' }, { text: 'O(n)' }, { text: 'Clones heap and extracts all n items non-destructively' }],
            [{ text: 'Search (Keyword scan)' }, { text: 'O(1)' }, { text: 'O(n)' }, { text: 'O(n)' }, { text: 'O(1)' }, { text: 'Linear vector traversal for substring or caller name matching' }]
        ];

        slide.addTable(perfData, {
            x: 0.6, y: 1.25, w: 8.8, h: 3.7,
            fontSize: 8.5, fontFace: 'Calibri',
            color: TEXT_WHITE,
            fill: { color: CARD_BG },
            border: { pt: 0.5, color: CARD_BORDER }
        });
    }

    // ==========================================
    // SLIDE 10: Limitations & Future Enhancements
    // ==========================================
    {
        const slide = setupSlide('Limitations and Future Enhancements', 'Operational Scope Boundaries & Road Map for City-Scale Upgrades', 10);

        // Left Card: Limitations
        slide.addShape(pres.ShapeType.roundRect, {
            x: 0.6, y: 1.2, w: 4.3, h: 3.8,
            fill: { color: CARD_BG },
            line: { color: ACCENT_RED, width: 1.2 },
            rectRadius: 0.1
        });
        slide.addText('⚠️ Current System Limitations', {
            x: 0.8, y: 1.35, w: 3.9, h: 0.3,
            fontSize: 12, fontFace: 'Calibri', bold: true, color: ACCENT_RED
        });
        slide.addText([
            { text: '1. Single-Terminal In-Memory Architecture: \n', options: { bold: true, color: TEXT_WHITE } },
            { text: 'The queue operates entirely in device RAM with CSV backup. Multi-station municipal networks require centralized socket synchronization.\n\n', options: { color: TEXT_MUTED } },
            { text: '2. Simulated GPS Coordinates: \n', options: { bold: true, color: TEXT_WHITE } },
            { text: 'Incident locations use predefined coordinate presets rather than active satellite triangulation or live vehicle OBD sensors.\n\n', options: { color: TEXT_MUTED } },
            { text: '3. In-App Notification Delivery: \n', options: { bold: true, color: TEXT_WHITE } },
            { text: 'Notifications are displayed inside the application UI feed rather than pushing outward to cellular telecom SMS gateways.', options: { color: TEXT_MUTED } }
        ], {
            x: 0.8, y: 1.75, w: 3.9, h: 3.1,
            fontSize: 9.5, fontFace: 'Calibri', lineSpacing: 13
        });

        // Right Card: Future Enhancements
        slide.addShape(pres.ShapeType.roundRect, {
            x: 5.1, y: 1.2, w: 4.3, h: 3.8,
            fill: { color: CARD_BG },
            line: { color: ACCENT_BLUE, width: 1.2 },
            rectRadius: 0.1
        });
        slide.addText('🚀 Planned Future Enhancements', {
            x: 5.3, y: 1.35, w: 3.9, h: 0.3,
            fontSize: 12, fontFace: 'Calibri', bold: true, color: ACCENT_BLUE
        });
        slide.addText([
            { text: '1. Live Fleet Tracking & Shortest-Path Routing: \n', options: { bold: true, color: TEXT_WHITE } },
            { text: 'Integrate OpenStreetMap SDK and Dijkstra / A* graph algorithms to calculate dynamic real-time traffic ETAs for ambulances.\n\n', options: { color: TEXT_MUTED } },
            { text: '2. Municipal Smart City IoT Integration: \n', options: { bold: true, color: TEXT_WHITE } },
            { text: 'Direct sensor feeds from automated fire alarms, road crash telematics, and water pipeline pressure gauges into the triage heap.\n\n', options: { color: TEXT_MUTED } },
            { text: '3. Multi-Tier Distributed Cloud Synchronization: \n', options: { bold: true, color: TEXT_WHITE } },
            { text: 'Deploying a distributed heap with Raft consensus for fault-tolerant multi-terminal police/fire dispatch centers.', options: { color: TEXT_MUTED } }
        ], {
            x: 5.3, y: 1.75, w: 3.9, h: 3.1,
            fontSize: 9.5, fontFace: 'Calibri', lineSpacing: 13
        });
    }

    // ==========================================
    // SLIDE 11: Conclusion & Key Learnings
    // ==========================================
    {
        const slide = setupSlide('Conclusion', 'Key Outcomes, Methodological Takeaways & Practical Impact', 11);

        const points = [
            {
                title: 'Problem Solved Successfully',
                text: 'Eliminated severe emergency dispatch delays caused by FIFO queues, replacing them with a logarithmic binary Max-Heap triage engine that prioritizes life-saving interventions.',
                color: ACCENT_GREEN
            },
            {
                title: 'Full Functional Application Built',
                text: 'Engineered a complete Computer-Aided Dispatch application featuring User Registration, Hospital-Grade Ambulance Screening, Dynamic Canvas Heap Visualizer, and CSV round-trip persistence.',
                color: ACCENT_GOLD
            },
            {
                title: 'Core DSA Competencies Mastered',
                text: 'Implemented 0-based array tree arithmetic, dual-key priority comparators, Floyd\'s O(n) Build-Heap algorithm, and auxiliary hash map indexing for O(log n) dynamic element updates.',
                color: ACCENT_BLUE
            },
            {
                title: 'Practical Impact & Competition Readiness',
                text: 'Demonstrated sub-millisecond execution across 200 concurrent operations, achieving high algorithmic efficiency, zero memory leaks, and 100% automated test suite pass rate.',
                color: ACCENT_RED
            }
        ];

        points.forEach((p, idx) => {
            const y = 1.25 + idx * 0.95;
            slide.addShape(pres.ShapeType.roundRect, {
                x: 0.6, y: y, w: 8.8, h: 0.85,
                fill: { color: CARD_BG },
                line: { color: p.color, width: 1.2 },
                rectRadius: 0.08
            });

            slide.addText(p.title, {
                x: 0.8, y: y + 0.1, w: 3.0, h: 0.25,
                fontSize: 11, fontFace: 'Calibri', bold: true, color: p.color
            });
            slide.addText(p.text, {
                x: 3.8, y: y + 0.1, w: 5.4, h: 0.65,
                fontSize: 9, fontFace: 'Calibri', color: TEXT_WHITE, lineSpacing: 12
            });
        });
    }

    // ==========================================
    // SLIDE 12: References & Q&A
    // ==========================================
    {
        const slide = setupSlide('References & Viva Voce Q&A', 'Academic Literature, Citations, Acknowledgements & Open Discussion', 12);

        // Left: References Box
        slide.addShape(pres.ShapeType.roundRect, {
            x: 0.6, y: 1.2, w: 4.8, h: 3.8,
            fill: { color: CARD_BG },
            line: { color: ACCENT_BLUE, width: 1.2 },
            rectRadius: 0.1
        });
        slide.addText('📚 Academic References & Standards', {
            x: 0.8, y: 1.35, w: 4.4, h: 0.3,
            fontSize: 12, fontFace: 'Calibri', bold: true, color: ACCENT_BLUE
        });
        slide.addText([
            { text: '1. Cormen, T. H., Leiserson, C. E., Rivest, R. L., & Stein, C. (2022). \n', options: { bold: true, color: TEXT_WHITE } },
            { text: '   Introduction to Algorithms (4th Edition). MIT Press. Chapter 6: Heapsort.\n\n', options: { color: TEXT_MUTED } },
            { text: '2. Sedgewick, R., & Wayne, K. (2014). \n', options: { bold: true, color: TEXT_WHITE } },
            { text: '   Algorithms (4th Edition). Addison-Wesley Professional. Priority Queues.\n\n', options: { color: TEXT_MUTED } },
            { text: '3. International Clinical Triage Standards: \n', options: { bold: true, color: TEXT_WHITE } },
            { text: '   Emergency Severity Index (ESI) & Canadian Triage and Acuity Scale (CTAS).\n\n', options: { color: TEXT_MUTED } },
            { text: '4. Android Jetpack & Modern Architecture Guidelines: \n', options: { bold: true, color: TEXT_WHITE } },
            { text: '   Google Android Developers Documentation: Jetpack Compose & 2D Graphics Canvas API.', options: { color: TEXT_MUTED } }
        ], {
            x: 0.8, y: 1.75, w: 4.4, h: 3.1,
            fontSize: 8.5, fontFace: 'Calibri', lineSpacing: 12
        });

        // Right: Acknowledgements & Thank You
        slide.addShape(pres.ShapeType.roundRect, {
            x: 5.6, y: 1.2, w: 3.8, h: 3.8,
            fill: { color: CARD_BG },
            line: { color: ACCENT_GOLD, width: 1.2 },
            rectRadius: 0.1
        });

        slide.addText('🙏 Acknowledgements', {
            x: 5.8, y: 1.35, w: 3.4, h: 0.3,
            fontSize: 12, fontFace: 'Calibri', bold: true, color: ACCENT_GOLD
        });
        slide.addText('We express our sincere gratitude to our esteemed Faculty Coordinator and the Head of Department, CSE-AIML, Lords Institute of Engineering & Technology, for their continuous mentorship, rigorous technical feedback, and invaluable guidance throughout Assignment-III.', {
            x: 5.8, y: 1.7, w: 3.4, h: 1.1,
            fontSize: 8.5, fontFace: 'Calibri', color: TEXT_MUTED, lineSpacing: 12
        });

        // Thank you & Q&A Box
        slide.addShape(pres.ShapeType.roundRect, {
            x: 5.8, y: 2.9, w: 3.4, h: 1.9,
            fill: { color: '0E1017' },
            line: { color: ACCENT_RED, width: 1 },
            rectRadius: 0.08
        });
        slide.addText('THANK YOU!', {
            x: 5.8, y: 3.15, w: 3.4, h: 0.45,
            fontSize: 22, fontFace: 'Calibri', bold: true, color: ACCENT_RED, align: 'center'
        });
        slide.addText('Questions & Answers\nWe welcome any technical inquiries, code walkthrough requests, and live viva demonstration queries.', {
            x: 5.9, y: 3.65, w: 3.2, h: 0.95,
            fontSize: 9, fontFace: 'Calibri', color: TEXT_WHITE, align: 'center', lineSpacing: 12
        });
    }

    const outputPath = path.join(__dirname, 'public', 'Emergency_Prioritizer_Presentation.pptx');
    await pres.writeFile({ fileName: outputPath });
    console.log(`Presentation generated successfully at: ${outputPath}`);
}

createPresentation().catch(err => {
    console.error('Error generating presentation:', err);
    process.exit(1);
});
