from pathlib import Path

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER
from reportlab.lib.pagesizes import letter
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import inch
from reportlab.platypus import (
    HRFlowable,
    KeepTogether,
    ListFlowable,
    ListItem,
    PageBreak,
    Paragraph,
    SimpleDocTemplate,
    Spacer,
    Table,
    TableStyle,
)


ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "HW3_StudentDealz_Project_Explanation.pdf"


def stylesheet():
    styles = getSampleStyleSheet()
    styles.add(
        ParagraphStyle(
            name="CoverTitle",
            parent=styles["Title"],
            fontName="Helvetica-Bold",
            fontSize=24,
            leading=30,
            textColor=colors.HexColor("#16324F"),
            alignment=TA_CENTER,
            spaceAfter=14,
        )
    )
    styles.add(
        ParagraphStyle(
            name="CoverSubtitle",
            parent=styles["BodyText"],
            fontName="Helvetica",
            fontSize=12,
            leading=17,
            textColor=colors.HexColor("#4B5563"),
            alignment=TA_CENTER,
            spaceAfter=8,
        )
    )
    styles.add(
        ParagraphStyle(
            name="Section",
            parent=styles["Heading1"],
            fontName="Helvetica-Bold",
            fontSize=15,
            leading=19,
            textColor=colors.HexColor("#16324F"),
            spaceBefore=12,
            spaceAfter=7,
        )
    )
    styles.add(
        ParagraphStyle(
            name="Subsection",
            parent=styles["Heading2"],
            fontName="Helvetica-Bold",
            fontSize=11.5,
            leading=15,
            textColor=colors.HexColor("#1F5C8B"),
            spaceBefore=8,
            spaceAfter=4,
        )
    )
    styles.add(
        ParagraphStyle(
            name="Body",
            parent=styles["BodyText"],
            fontName="Helvetica",
            fontSize=10,
            leading=14.2,
            textColor=colors.HexColor("#111827"),
            spaceAfter=6,
        )
    )
    styles.add(
        ParagraphStyle(
            name="Small",
            parent=styles["BodyText"],
            fontName="Helvetica",
            fontSize=8.4,
            leading=11.6,
            textColor=colors.HexColor("#374151"),
        )
    )
    styles.add(
        ParagraphStyle(
            name="CodeBlock",
            parent=styles["BodyText"],
            fontName="Courier",
            fontSize=8,
            leading=10.5,
            textColor=colors.HexColor("#111827"),
            backColor=colors.HexColor("#F3F4F6"),
            borderPadding=5,
            spaceBefore=3,
            spaceAfter=7,
        )
    )
    return styles


def bullet_list(items, styles):
    return ListFlowable(
        [ListItem(Paragraph(item, styles["Body"]), leftIndent=12) for item in items],
        bulletType="bullet",
        start="circle",
        leftIndent=16,
        bulletFontName="Helvetica",
        bulletFontSize=7,
    )


def requirement_table(styles):
    data = [
        [
            Paragraph("<b>Assignment requirement</b>", styles["Small"]),
            Paragraph("<b>Where it is addressed</b>", styles["Small"]),
            Paragraph("<b>Status</b>", styles["Small"]),
        ],
        [
            Paragraph("Firebase Analytics", styles["Small"]),
            Paragraph(
                "Firebase Analytics dependency is declared in <b>app/build.gradle</b>. "
                "The app obtains a FirebaseAnalytics instance in <b>MainActivity.java</b>.",
                styles["Small"],
            ),
            Paragraph("<b>Completed</b>", styles["Small"]),
        ],
        [
            Paragraph("Firebase Crashlytics", styles["Small"]),
            Paragraph(
                "The Crashlytics Gradle plugin and dependency are declared in <b>app/build.gradle</b>. "
                "The debug build runs Crashlytics Gradle tasks successfully.",
                styles["Small"],
            ),
            Paragraph("<b>Completed</b>", styles["Small"]),
        ],
        [
            Paragraph("Dynamic data in Firebase Firestore", styles["Small"]),
            Paragraph(
                "Deals are read from the Firestore <b>Deals</b> collection using snapshot listeners. "
                "Student accounts are saved in the <b>Students</b> collection.",
                styles["Small"],
            ),
            Paragraph("<b>Completed</b>", styles["Small"]),
        ],
        [
            Paragraph("Bonus: third-party library", styles["Small"]),
            Paragraph(
                "Google ML Kit Text Recognition is used to read text from a student ID image during signup.",
                styles["Small"],
            ),
            Paragraph("<b>Completed</b>", styles["Small"]),
        ],
    ]

    table = Table(data, colWidths=[1.45 * inch, 4.0 * inch, 1.1 * inch], hAlign="LEFT")
    table.setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#E8F1F8")),
                ("TEXTCOLOR", (0, 0), (-1, 0), colors.HexColor("#16324F")),
                ("GRID", (0, 0), (-1, -1), 0.45, colors.HexColor("#CBD5E1")),
                ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
                ("LEFTPADDING", (0, 0), (-1, -1), 7),
                ("RIGHTPADDING", (0, 0), (-1, -1), 7),
                ("TOPPADDING", (0, 0), (-1, -1), 7),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 7),
                ("BACKGROUND", (0, 1), (-1, -1), colors.white),
            ]
        )
    )
    return table


def code(text, styles):
    escaped = (
        text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\n", "<br/>")
    )
    return Paragraph(escaped, styles["CodeBlock"])


def build_story(styles):
    story = []

    story.extend(
        [
            Spacer(1, 0.65 * inch),
            Paragraph("StudentDealz", styles["CoverTitle"]),
            Paragraph("HW3 Project Explanation", styles["CoverSubtitle"]),
            Paragraph(
                "Advanced Topics in Apps Innovations - Firebase Integration Assignment",
                styles["CoverSubtitle"],
            ),
            Spacer(1, 0.25 * inch),
            HRFlowable(width="85%", color=colors.HexColor("#CBD5E1"), thickness=1),
            Spacer(1, 0.25 * inch),
            Paragraph("<b>Team members:</b> ________________________________", styles["Body"]),
            Paragraph("<b>GitHub link:</b> ________________________________", styles["Body"]),
            Paragraph("<b>Project:</b> Native Android application written in Java", styles["Body"]),
            Paragraph("<b>Package:</b> com.example.studentdealz", styles["Body"]),
            Spacer(1, 0.25 * inch),
            Paragraph("Overview", styles["Section"]),
            Paragraph(
                "StudentDealz is an Android application for students to register, log in, and browse student discounts. "
                "For HW3, the project was connected to Firebase services and extended with dynamic cloud data. "
                "The app includes Firebase Analytics, Firebase Crashlytics, Firebase Firestore, Firebase Authentication, "
                "and a bonus third-party library: Google ML Kit Text Recognition.",
                styles["Body"],
            ),
            Spacer(1, 0.1 * inch),
            requirement_table(styles),
            PageBreak(),
        ]
    )

    story.extend(
        [
            Paragraph("Firebase Setup", styles["Section"]),
            Paragraph(
                "The Firebase project is connected to the Android app through the Google Services configuration file "
                "<b>app/google-services.json</b>. The Gradle build uses the Google Services plugin so Firebase can read "
                "the project configuration during the Android build.",
                styles["Body"],
            ),
            code(
                "plugins {\n"
                "    alias(libs.plugins.android.application)\n"
                "    alias(libs.plugins.google.gms.google.services)\n"
                "    alias(libs.plugins.google.firebase.crashlytics)\n"
                "}",
                styles,
            ),
            Paragraph("Main Firebase dependencies", styles["Subsection"]),
            code(
                "implementation libs.firebase.analytics\n"
                "implementation libs.firebase.crashlytics\n"
                "implementation libs.firebase.firestore\n"
                "implementation libs.firebase.auth",
                styles,
            ),
            Paragraph("Firebase Analytics", styles["Section"]),
            Paragraph(
                "The app includes Firebase Analytics to connect user activity tracking to Firebase. Analytics is declared "
                "as a dependency and initialized in the main screen of the app.",
                styles["Body"],
            ),
            code(
                "import com.google.firebase.analytics.FirebaseAnalytics;\n\n"
                "private FirebaseAnalytics mFirebaseAnalytics;\n\n"
                "mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);",
                styles,
            ),
            Paragraph(
                "This connects the application to Firebase Analytics. Firebase can then collect standard app analytics "
                "events such as app opens, screen engagement, and other default analytics data.",
                styles["Body"],
            ),
            Paragraph("Firebase Crashlytics", styles["Section"]),
            Paragraph(
                "Crashlytics is connected through both the Gradle plugin and the Firebase Crashlytics dependency. "
                "This allows Firebase to receive crash reports if the app crashes on a user device.",
                styles["Body"],
            ),
            code(
                "alias(libs.plugins.google.firebase.crashlytics)\n"
                "implementation libs.firebase.crashlytics",
                styles,
            ),
            Paragraph(
                "During the debug build, the project ran Crashlytics build tasks successfully, including mapping file and "
                "version control metadata injection tasks.",
                styles["Body"],
            ),
        ]
    )

    story.append(PageBreak())
    story.extend(
        [
            Paragraph("Firebase Firestore", styles["Section"]),
            Paragraph(
                "Firestore is used for dynamic data. Instead of relying only on hard-coded local discount data, the app "
                "listens to the Firestore <b>Deals</b> collection and updates the UI when cloud data changes.",
                styles["Body"],
            ),
            code(
                "FirebaseFirestore.getInstance()\n"
                "        .collection(\"Deals\")\n"
                "        .addSnapshotListener(createDealsListener(context, callback, null));",
                styles,
            ),
            Paragraph(
                "Category screens also query Firestore dynamically by category:",
                styles["Body"],
            ),
            code(
                "FirebaseFirestore.getInstance()\n"
                "        .collection(\"Deals\")\n"
                "        .whereEqualTo(\"category\", category)\n"
                "        .addSnapshotListener(createDealsListener(context, callback, category));",
                styles,
            ),
            Paragraph(
                "Each deal document is converted into an Item object using the fields <b>discount</b>, <b>partner</b>, "
                "<b>category</b>, and <b>imageName</b>. If Firestore data is unavailable, the app falls back to local sample "
                "deals so the UI remains usable.",
                styles["Body"],
            ),
            Paragraph("Student Data in Firestore", styles["Subsection"]),
            Paragraph(
                "The signup flow also uses Firestore. Before account creation, the app checks whether the student ID "
                "already exists in the <b>Students</b> collection. After Firebase Authentication creates the user, the "
                "student details are saved in Firestore under the user's UID.",
                styles["Body"],
            ),
            code(
                "FirebaseFirestore.getInstance()\n"
                "        .collection(\"Students\")\n"
                "        .document(user.getUid())\n"
                "        .set(student.getAsMap());",
                styles,
            ),
            Paragraph("Bonus: Third-Party Library", styles["Section"]),
            Paragraph(
                "For the bonus requirement, the project uses <b>Google ML Kit Text Recognition</b>. This is a third-party "
                "library from Google that performs OCR on images. In StudentDealz, it is used during signup to read text "
                "from a student ID photo and extract details automatically.",
                styles["Body"],
            ),
            code("implementation libs.mlkit.text.recognition", styles),
            code(
                "TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)\n"
                "        .process(image)\n"
                "        .addOnSuccessListener(visionText -> {\n"
                "            StudentIdOcrParser.ExtractedDetails details =\n"
                "                    StudentIdOcrParser.parse(visionText.getText());\n"
                "            openConfirmStudentDetails(details, imageUri);\n"
                "        });",
                styles,
            ),
            Paragraph(
                "This improves the registration flow because students can upload or take a photo of their student ID, "
                "and the app attempts to read the name, ID number, and institution from the image.",
                styles["Body"],
            ),
        ]
    )

    story.append(PageBreak())
    story.extend(
        [
            Paragraph("Implementation Summary", styles["Section"]),
            bullet_list(
                [
                    "Created and connected a Firebase project to the Android app using google-services.json.",
                    "Added Google Services and Crashlytics Gradle plugins.",
                    "Added Firebase Analytics, Crashlytics, Firestore, and Auth dependencies.",
                    "Initialized Firebase Analytics in MainActivity.",
                    "Connected Crashlytics through Gradle and the Firebase dependency.",
                    "Loaded deals dynamically from the Firestore Deals collection.",
                    "Queried deals by category from Firestore.",
                    "Stored registered student data in the Firestore Students collection.",
                    "Used Firebase Authentication for account creation, login, logout, and session checks.",
                    "Implemented the bonus requirement using Google ML Kit Text Recognition for OCR.",
                ],
                styles,
            ),
            Spacer(1, 0.1 * inch),
            Paragraph("Main Files", styles["Section"]),
            bullet_list(
                [
                    "<b>app/build.gradle</b> - Firebase plugins and dependencies.",
                    "<b>gradle/libs.versions.toml</b> - Firebase and ML Kit library versions.",
                    "<b>app/google-services.json</b> - Firebase project configuration.",
                    "<b>MainActivity.java</b> - Firebase Analytics initialization and dynamic deal list.",
                    "<b>DealRepository.java</b> - Firestore dynamic deal loading and category filtering.",
                    "<b>UserRepository.java</b> - Firebase Auth and Firestore student data storage.",
                    "<b>StudentIdPhotoActivity.java</b> - ML Kit OCR flow for student ID images.",
                    "<b>StudentIdOcrParser.java</b> - Parses recognized OCR text into student details.",
                ],
                styles,
            ),
            Spacer(1, 0.1 * inch),
            Paragraph("Build Verification", styles["Section"]),
            Paragraph(
                "The project was verified by running a debug build with Java 21:",
                styles["Body"],
            ),
            code("JAVA_HOME=/Users/liaalma/Library/Java/JavaVirtualMachines/ms-21.0.11/Contents/Home ./gradlew assembleDebug", styles),
            Paragraph(
                "Result: <b>BUILD SUCCESSFUL</b>. This confirms that the project compiles with the Firebase and ML Kit "
                "dependencies included.",
                styles["Body"],
            ),
            Spacer(1, 0.1 * inch),
            KeepTogether(
                [
                    Paragraph("Conclusion", styles["Section"]),
                    Paragraph(
                        "StudentDealz satisfies the HW3 assignment requirements. The app is connected to Firebase "
                        "Analytics, Firebase Crashlytics, and Firebase Firestore, and it also includes a third-party "
                        "library bonus through Google ML Kit Text Recognition.",
                        styles["Body"],
                    ),
                ]
            ),
        ]
    )

    return story


def footer(canvas, doc):
    canvas.saveState()
    canvas.setFont("Helvetica", 8)
    canvas.setFillColor(colors.HexColor("#6B7280"))
    canvas.drawString(0.72 * inch, 0.45 * inch, "StudentDealz - HW3 Firebase Project Explanation")
    canvas.drawRightString(7.78 * inch, 0.45 * inch, f"Page {doc.page}")
    canvas.restoreState()


def main():
    styles = stylesheet()
    doc = SimpleDocTemplate(
        str(OUTPUT),
        pagesize=letter,
        rightMargin=0.72 * inch,
        leftMargin=0.72 * inch,
        topMargin=0.68 * inch,
        bottomMargin=0.68 * inch,
        title="StudentDealz HW3 Project Explanation",
        author="StudentDealz Team",
        subject="Firebase integration assignment explanation",
    )
    doc.build(build_story(styles), onFirstPage=footer, onLaterPages=footer)
    print(OUTPUT)


if __name__ == "__main__":
    main()
