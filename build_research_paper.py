from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
from docx import Document
from docx.enum.section import WD_SECTION
from docx.enum.table import WD_CELL_VERTICAL_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_BREAK
from docx.shared import Inches, Pt, RGBColor
from docx.oxml import OxmlElement
from docx.oxml.ns import qn

ROOT = Path(__file__).resolve().parent
OUT_DOCX = ROOT / "AgriPlus_Coffee_Leaf_Health_Research_Paper.docx"
TMP = ROOT / "tmp" / "pdfs"
TMP.mkdir(parents=True, exist_ok=True)


def font(name, size):
    try:
        return ImageFont.truetype(f"C:/Windows/Fonts/{name}.ttf", size)
    except OSError:
        return ImageFont.load_default()


def make_architecture_figure(path):
    width, height = 1800, 480
    image = Image.new("RGB", (width, height), "white")
    draw = ImageDraw.Draw(image)
    title_font = font("arialbd", 36)
    body_font = font("arial", 30)
    small_font = font("arial", 27)
    navy, blue, pale, ink, line = "#173A59", "#2877B8", "#EEF6FC", "#22384C", "#9CB7CC"
    boxes = [
        (30, "Capture and precheck", "Camera or gallery\nQuality feedback"),
        (475, "Remote inference", "JPEG upload\nRoboflow endpoint"),
        (920, "Screening result", "Class and score\nCare information"),
        (1365, "Farm workspace", "History and tools\nFirebase and local data"),
    ]
    box_w, box_h, y0 = 390, 190, 80
    for x, heading, detail in boxes:
        draw.rounded_rectangle((x, y0, x + box_w, y0 + box_h), radius=24, fill=pale, outline=line, width=4)
        draw.rounded_rectangle((x, y0, x + box_w, y0 + 56), radius=22, fill=navy)
        draw.rectangle((x, y0 + 34, x + box_w, y0 + 56), fill=navy)
        draw.text((x + 18, y0 + 10), heading, font=title_font, fill="white")
        for j, row in enumerate(detail.split("\n")):
            draw.text((x + 20, y0 + 82 + j * 45), row, font=body_font, fill=ink)
    for x in (430, 875, 1320):
        y = y0 + box_h // 2
        draw.line((x, y, x + 42, y), fill=blue, width=8)
        draw.polygon([(x + 42, y - 16), (x + 66, y), (x + 42, y + 16)], fill=blue)
    draw.text((32, 320), "Cross-cutting services: weather feed and rule-based advisories; plantation, treatment, reminder and consultation screens", font=small_font, fill=ink)
    image.save(path, quality=95)


def set_cell_shading(cell, fill):
    tc_pr = cell._tc.get_or_add_tcPr()
    shd = tc_pr.find(qn("w:shd"))
    if shd is None:
        shd = OxmlElement("w:shd")
        tc_pr.append(shd)
    shd.set(qn("w:fill"), fill)


def set_cell_margins(cell, top=90, start=95, bottom=90, end=95):
    tc = cell._tc
    tc_pr = tc.get_or_add_tcPr()
    tc_mar = tc_pr.first_child_found_in("w:tcMar")
    if tc_mar is None:
        tc_mar = OxmlElement("w:tcMar")
        tc_pr.append(tc_mar)
    for edge, value in (("top", top), ("start", start), ("bottom", bottom), ("end", end)):
        node = tc_mar.find(qn(f"w:{edge}"))
        if node is None:
            node = OxmlElement(f"w:{edge}")
            tc_mar.append(node)
        node.set(qn("w:w"), str(value))
        node.set(qn("w:type"), "dxa")


def set_table_borders(table, color="B7C5D0", size="5"):
    tbl_pr = table._tbl.tblPr
    borders = tbl_pr.first_child_found_in("w:tblBorders")
    if borders is None:
        borders = OxmlElement("w:tblBorders")
        tbl_pr.append(borders)
    for edge in ("top", "left", "bottom", "right", "insideH", "insideV"):
        tag = qn(f"w:{edge}")
        el = borders.find(tag)
        if el is None:
            el = OxmlElement(f"w:{edge}")
            borders.append(el)
        el.set(qn("w:val"), "single")
        el.set(qn("w:sz"), size)
        el.set(qn("w:space"), "0")
        el.set(qn("w:color"), color)


def set_repeat_table_header(row):
    tr_pr = row._tr.get_or_add_trPr()
    tbl_header = OxmlElement("w:tblHeader")
    tbl_header.set(qn("w:val"), "true")
    tr_pr.append(tbl_header)


def set_column_layout(section, count=2, space=360):
    sect_pr = section._sectPr
    cols = sect_pr.find(qn("w:cols"))
    if cols is None:
        cols = OxmlElement("w:cols")
        sect_pr.append(cols)
    cols.set(qn("w:num"), str(count))
    cols.set(qn("w:space"), str(space))
    cols.set(qn("w:equalWidth"), "1")


def set_page_field(paragraph):
    run = paragraph.add_run()
    begin = OxmlElement("w:fldChar")
    begin.set(qn("w:fldCharType"), "begin")
    instr = OxmlElement("w:instrText")
    instr.set(qn("xml:space"), "preserve")
    instr.text = " PAGE "
    separate = OxmlElement("w:fldChar")
    separate.set(qn("w:fldCharType"), "separate")
    text = OxmlElement("w:t")
    text.text = "1"
    end = OxmlElement("w:fldChar")
    end.set(qn("w:fldCharType"), "end")
    for el in (begin, instr, separate, text, end):
        run._r.append(el)


doc = Document()
section = doc.sections[0]
section.page_width = Inches(8.27)
section.page_height = Inches(11.69)
section.top_margin = Inches(0.62)
section.bottom_margin = Inches(0.58)
section.left_margin = Inches(0.66)
section.right_margin = Inches(0.66)
section.header_distance = Inches(0.28)
section.footer_distance = Inches(0.26)

normal = doc.styles["Normal"]
normal.font.name = "Times New Roman"
normal.font.size = Pt(11.0)
normal.font.color.rgb = RGBColor(0, 0, 0)
normal._element.rPr.rFonts.set(qn("w:ascii"), "Times New Roman")
normal._element.rPr.rFonts.set(qn("w:hAnsi"), "Times New Roman")
normal.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
normal.paragraph_format.line_spacing = 1.06
normal.paragraph_format.space_after = Pt(4.0)
normal.paragraph_format.first_line_indent = Inches(0.13)
normal.paragraph_format.widow_control = True

for style_name, size in (("Title", 19), ("Heading 1", 9.5), ("Heading 2", 8.85)):
    style = doc.styles[style_name]
    style.font.name = "Times New Roman"
    style.font.size = Pt(size)
    style.font.bold = True
    style.font.color.rgb = RGBColor(0, 0, 0)
    style._element.rPr.rFonts.set(qn("w:ascii"), "Times New Roman")
    style._element.rPr.rFonts.set(qn("w:hAnsi"), "Times New Roman")
    style.paragraph_format.keep_with_next = True
    style.paragraph_format.space_before = Pt(6 if style_name == "Heading 1" else 4)
    style.paragraph_format.space_after = Pt(3 if style_name == "Heading 1" else 2)

footer = section.footer.paragraphs[0]
footer.alignment = WD_ALIGN_PARAGRAPH.CENTER
footer.paragraph_format.space_before = Pt(0)
footer.paragraph_format.space_after = Pt(0)
footer.add_run("AgriPlus project paper  |  ")
set_page_field(footer)
for run in footer.runs:
    run.font.name = "Times New Roman"
    run.font.size = Pt(7.5)
    run.font.color.rgb = RGBColor(90, 100, 110)

title = doc.add_paragraph(style="Title")
title.alignment = WD_ALIGN_PARAGRAPH.CENTER
title.paragraph_format.space_before = Pt(2)
title.paragraph_format.space_after = Pt(4)
title.paragraph_format.first_line_indent = Inches(0)
title.add_run("AgriPlus Mobile Decision Support for Coffee Leaf Health Screening and Farm Management")

author = doc.add_paragraph()
author.alignment = WD_ALIGN_PARAGRAPH.CENTER
author.paragraph_format.first_line_indent = Inches(0)
author.paragraph_format.space_after = Pt(1)
author.paragraph_format.keep_with_next = True
r = author.add_run("Akshay Kumar A")
r.bold = True
r.font.size = Pt(9.5)

affil = doc.add_paragraph()
affil.alignment = WD_ALIGN_PARAGRAPH.CENTER
affil.paragraph_format.first_line_indent = Inches(0)
affil.paragraph_format.space_after = Pt(5)
affil.paragraph_format.keep_with_next = True
affil.add_run("[Department, Institution, City, Country]  |  [Corresponding author email]").font.size = Pt(8.3)

affil.clear()
affil.add_run(
    "Department of Master of Computer Applications, Nitte (Deemed to be University), "
    "NMAM Institute of Technology (NMAMIT), Nitte, India\nakshayckm05@gmail.com"
).font.size = Pt(8.3)

mentor = doc.add_paragraph()
mentor.alignment = WD_ALIGN_PARAGRAPH.CENTER
mentor.paragraph_format.first_line_indent = Inches(0)
mentor.paragraph_format.space_after = Pt(5)
mentor.paragraph_format.keep_with_next = True
mentor_run = mentor.add_run(
    "Faculty Mentor: Mamatha Balipa | Department of MCA, Nitte (Deemed to be University), "
    "NMAM Institute of Technology (NMAMIT), Nitte, India | mamathabalipa@nitte.edu.in"
)
mentor_run.font.size = Pt(8.0)

abstract = doc.add_paragraph()
abstract.paragraph_format.first_line_indent = Inches(0)
abstract.paragraph_format.space_after = Pt(3)
abstract.paragraph_format.keep_together = True
run = abstract.add_run("Abstract—")
run.bold = True
abstract.add_run(
    "Coffee leaf disorders are difficult to assess consistently when growers lack immediate access to diagnostic support. "
    "This paper presents AgriPlus, an Android decision-support prototype that combines image-based leaf screening with tools for plantation records, weather context, treatment information, reminders, and scan history. "
    "The application uses Kotlin and Jetpack Compose, Google authentication through Firebase, a remote image-inference endpoint, and user-scoped cloud persistence. "
    "A lightweight image precheck reports extreme brightness, low local contrast, and recently repeated image patterns; a confidence threshold controls whether a remote prediction is displayed. "
    "The contribution is an implementation-level account of how screening is embedded in a broader farm workflow, together with an artifact-based assessment of what the current code demonstrates and what remains unverified. "
    "Source inspection confirms the active scan path depends on network inference; a TensorFlow Lite model is present in the assets but is not connected to that path. "
    "No labeled evaluation dataset, independent classifier metrics, farmer study, or expert diagnostic validation was available in the project artifact. "
    "Accordingly, this paper reports a functional prototype rather than model accuracy or clinical agronomic effectiveness, and defines a field-validation protocol needed before operational deployment."
)
for r in abstract.runs:
    r.font.size = Pt(8.15)

keywords = doc.add_paragraph()
keywords.paragraph_format.first_line_indent = Inches(0)
keywords.paragraph_format.space_after = Pt(4)
kw = keywords.add_run("Index Terms—")
kw.bold = True
keywords.add_run("coffee leaf screening, mobile decision support, plant disease recognition, Android, image quality, precision agriculture")
for r in keywords.runs:
    r.font.size = Pt(8.1)

two_col = doc.add_section(WD_SECTION.CONTINUOUS)
two_col.page_width = Inches(8.27)
two_col.page_height = Inches(11.69)
two_col.top_margin = Inches(0.62)
two_col.bottom_margin = Inches(0.58)
two_col.left_margin = Inches(0.66)
two_col.right_margin = Inches(0.66)
two_col.header_distance = Inches(0.28)
two_col.footer_distance = Inches(0.26)
set_column_layout(two_col)
two_col.footer.is_linked_to_previous = True


def heading(text, level=1):
    p = doc.add_paragraph(style="Heading 1" if level == 1 else "Heading 2")
    p.paragraph_format.first_line_indent = Inches(0)
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER if level == 1 else WD_ALIGN_PARAGRAPH.LEFT
    p.add_run(text)
    return p


def para(text, keep=False):
    p = doc.add_paragraph()
    p.add_run(text)
    if keep:
        p.paragraph_format.keep_together = True
    return p


def caption(text):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.first_line_indent = Inches(0)
    p.paragraph_format.space_before = Pt(2)
    p.paragraph_format.space_after = Pt(3)
    p.paragraph_format.keep_with_next = True
    r = p.add_run(text)
    r.font.size = Pt(7.7)
    return p


heading("I INTRODUCTION")
para(
    "Coffee production supports rural livelihoods across tropical regions, while foliar disease and pest symptoms can reduce plant vigor and complicate farm management. "
    "Visual diagnosis is a practical first step, but symptom appearance varies with cultivar, disease stage, lighting, leaf orientation, and background. "
    "A phone-based workflow can make image screening more accessible; however, an application is useful only when its predictions are presented with appropriate uncertainty and connected to decisions that growers can act on."
)
para(
    "Deep convolutional networks have established the technical feasibility of image-based plant disease recognition. "
    "Mohanty et al. reported 99.35% accuracy on a held-out split of the controlled PlantVillage collection, but accuracy fell to 31.4% on images gathered from external online sources, illustrating the sensitivity of image models to acquisition context [1]. "
    "Coffee-specific datasets address part of this gap: RoCoLe contains smartphone images acquired under field conditions and expert-reviewed labels for healthy leaves, mites, and rust severity [2], while JMuBEN and JMuBEN2 provide a larger Arabica image collection across five classes [3]. "
    "These datasets make field-oriented testing possible; they do not establish that a separately hosted model generalizes to a new region or camera."
)
para(
    "AgriPlus is an Android prototype developed around that deployment problem. It accepts a leaf image, applies a fast image-quality warning, requests a prediction from a hosted service, translates recognized labels into readable information, and stores scan history. "
    "The same application also includes weather advisory, plantation, treatment library, reminder, consultation, analytics, and scan-comparison screens. "
    "This paper documents the implemented workflow and distinguishes implemented behavior from evidence that is not available in the repository."
)
para(
    "The paper makes three contributions. First, it describes the current mobile-to-cloud screening and farm-management architecture at implementation level. "
    "Second, it identifies operational safeguards and risks visible in the code, including a 0.60 confidence cutoff, non-blocking quality warnings, Base64 image history, and a client-side inference credential. "
    "Third, it specifies a reproducible validation plan that separates classifier accuracy, field robustness, software reliability, and user-centered outcomes. "
    "The paper does not claim a new neural network, a new dataset, or measured disease-detection performance."
)

heading("II RELATED WORK")
heading("A Plant Disease Recognition and Domain Shift", 2)
para(
    "Prior image-based disease studies often report results on curated image collections. The external-image result in [1] demonstrates why a high random-split score cannot be treated as field accuracy. "
    "Model evaluation should reflect the intended deployment setting, with separation by plant, farm, or collection session where repeated views could otherwise leak visual information across training and test data. "
    "Ferentinos examined convolutional models for detecting and diagnosing disease from plant images, establishing a broad methodological foundation for automated image screening [6]. "
    "For a farm-facing product, classifier metrics must be paired with measures of calibration, rejection behavior, and performance under realistic capture conditions."
)
heading("B Coffee Datasets and Mobile Systems", 2)
para(
    "Coffee-specific resources differ in species, collection protocol, class design, and annotation depth. RoCoLe reports 1,560 smartphone photographs from 390 robusta plants, with six labels including four visible rust-severity levels and expert-reviewed ground truth [2]. "
    "The JMuBEN datasets include 58,555 Arabica leaf images across Phoma, Cercospora, rust, miner, and healthy categories, collected in Kenya with pathology support [3]. "
    "Tassis et al. combined leaf segmentation and disease/pest recognition using smartphone images captured in natural scenes, addressing clutter and lighting variation that controlled-image benchmarks do not represent [4]. "
    "Hitimana et al. describe a coffee reporting application that integrates a deep-learning workflow with location-aware disease reporting and farmer-oriented information management [5]."
)
para(
    "AgriPlus shares the mobile workflow objective but has a narrower evidence base: the project repository exposes an inference endpoint and client integration, not its training corpus, model architecture, class-wise metrics, or model-card documentation. "
    "It therefore cannot be compared numerically with [1]–[5]. Its contribution is the surrounding Android workflow and a transparent account of its current limitations, rather than a competing model benchmark."
)
para(
    "These studies point to complementary design needs. Curated benchmarks are useful for controlled model development, field image collections are needed to measure robustness to natural acquisition variation, and mobile reporting systems must preserve provenance to connect an image and farm context to follow-up. A deployed application should state which of these functions it supports and which remain outside its evidence. "
    "AgriPlus currently performs local image preparation and remote classification, while its repository does not expose a validated dataset or reproducible training procedure. That boundary shapes the claims that can responsibly be made about the prototype."
)

heading("III SYSTEM DESIGN AND METHODS")
heading("A Artifact-Based Study", 2)
para(
    "This work uses a design-and-implementation case study of the AgriPlus source artifact. The Android navigation graph, scan flow, image-quality utility, API client, Firebase data manager, and feature-specific repositories were inspected to trace data movement and user-visible behavior. "
    "A debug build and launch on one connected handset were used as a smoke check of packaging and startup. This check does not constitute systematic functional testing, performance measurement, user evaluation, or validation of agricultural advice."
)
para(
    "The analysis records only behavior observable in code. The repository does not include labeled coffee images, model-training scripts, a description of the deployed model, a locked API response corpus, or a field-test report. "
    "No accuracy, precision, recall, F1 score, inference latency, or farmer usability result is therefore estimated in this study."
)

heading("B Application Workflow")
para(
    "The application is organized as a Kotlin single-activity Android client using Jetpack Compose and Navigation Compose. Google sign-in is exchanged for a Firebase Authentication session. A signed-in user's history is addressed under a UID-specific Realtime Database path. "
    "From the home screen, the user captures a camera preview or selects a gallery image. The image is checked locally for basic quality cues and then uploaded as JPEG data to a hosted inference endpoint when the user starts a scan."
)

figure_path = TMP / "agriplus_architecture.png"
make_architecture_figure(figure_path)
fig_p = doc.add_paragraph()
fig_p.alignment = WD_ALIGN_PARAGRAPH.CENTER
fig_p.paragraph_format.first_line_indent = Inches(0)
fig_p.paragraph_format.space_before = Pt(2)
fig_p.paragraph_format.space_after = Pt(0)
fig_p.add_run().add_picture(str(figure_path), width=Inches(3.35))
caption("Fig. 1. Implemented AgriPlus workflow; local farm tools and weather context surround the network-dependent image-inference path.")

heading("C Image Intake and Quality Feedback", 2)
para(
    "The image checker downsamples each bitmap to 64 × 64 pixels. It computes mean luminance and flags values below 42 or above 228 on an 8-bit scale, then estimates local sharpness from neighboring-pixel luminance differences and flags an average gradient below 5.2. "
    "A 64-bit luminance-pattern hash is compared with hashes from recent selections in the current screen session to warn about repeats. These are inexpensive heuristics, not a validated image-quality model. "
    "The current screen displays the warning but does not use the returned validity flag to prevent a scan; the quality check is advisory rather than a hard gate. The preview camera contract also returns a reduced-resolution image."
)

heading("D Remote Prediction and Result Mapping", 2)
para(
    "Before upload, the selected bitmap is JPEG-compressed at maximum quality for the inference request. The client posts the image to a versioned Roboflow serverless endpoint using OkHttp and parses the first prediction's class label and confidence from JSON. "
    "Predictions below 0.60 are rejected by the user interface with a request to recapture the image. For accepted results, a local disease repository maps known labels to a name, cause, symptoms, and static treatment text; unmatched labels fall back to a generic unknown-condition explanation. "
    "The threshold is a code constant, not a threshold selected through calibration analysis, and the returned confidence is not demonstrated to be a calibrated probability."
)
para(
    "A TensorFlow Lite model and labels file are packaged in the application, and a LeafClassifier class implements local inference. The active HomeScreen scan path calls the remote API client instead of that classifier. "
    "Accordingly, the presence of model assets does not establish offline screening: image classification currently depends on network reachability and the remote service."
)

heading("IV IMPLEMENTATION")
para(
    "The artifact review followed the path from visible screen action to network or persistence effect: entry-point navigation was checked, the selected image was followed through the quality utility and request builder, response fields were traced to the displayed result, and history writes were inspected. Feature modules were reviewed for their data source and fallback behavior. "
    "This procedure helps describe integration boundaries and locate mismatches between interface labels and code behavior, but it is not a formal static-analysis audit. No claim is made that every execution path or backend configuration was exhaustively verified."
)
para(
    "The implementation combines a presentation layer of Compose screens with screen-level state and helper/data classes. It is a feature-rich prototype rather than a strict clean-architecture or ViewModel-based system. "
    "The main scan orchestration, response parsing, and result display reside in the home screen; separate managers handle some persistence and external data retrieval. Table I summarizes the implemented feature groups and their operational boundaries."
)

caption("TABLE I  IMPLEMENTED FEATURE GROUPS AND CURRENT BOUNDARIES")
table = doc.add_table(rows=1, cols=3)
table.autofit = False
widths = [Inches(0.68), Inches(1.34), Inches(1.22)]
tbl_w = table._tbl.tblPr.find(qn("w:tblW"))
if tbl_w is None:
    tbl_w = OxmlElement("w:tblW")
    table._tbl.tblPr.append(tbl_w)
tbl_w.set(qn("w:w"), "4666")
tbl_w.set(qn("w:type"), "dxa")
for i, text in enumerate(("Feature", "Implementation", "Boundary")):
    cell = table.rows[0].cells[i]
    cell.width = widths[i]
    cell.text = text
    set_cell_shading(cell, "173A59")
    cell.vertical_alignment = WD_CELL_VERTICAL_ALIGNMENT.CENTER
    set_cell_margins(cell)
    for p in cell.paragraphs:
        p.alignment = WD_ALIGN_PARAGRAPH.LEFT
        p.paragraph_format.first_line_indent = Inches(0)
        p.paragraph_format.space_after = Pt(0)
        for run in p.runs:
            run.font.name = "Times New Roman"
            run.font.size = Pt(7.2)
            run.font.bold = True
            run.font.color.rgb = RGBColor(255, 255, 255)

rows = [
    ("Leaf screening", "Camera/gallery; local warnings; remote label and score", "No disclosed model card or measured accuracy"),
    ("History", "UID-scoped Firebase records; JPEG stored as Base64", "Payload size and retention require review"),
    ("Farm tools", "Plantations, reminders, treatment notes, analytics", "Local preferences and cloud sync coexist"),
    ("Weather", "Open-Meteo forecast and rule-based coffee advisory", "Fallback weather is synthetic, not live"),
    ("Consultation", "Keyword/category rules generate preliminary text", "No human agronomist response is implemented"),
]
for ridx, row in enumerate(rows):
    cells = table.add_row().cells
    for i, text in enumerate(row):
        cells[i].width = widths[i]
        cells[i].text = text
        cells[i].vertical_alignment = WD_CELL_VERTICAL_ALIGNMENT.CENTER
        set_cell_margins(cells[i])
        if ridx % 2 == 1:
            set_cell_shading(cells[i], "F0F6FA")
        for p in cells[i].paragraphs:
            p.alignment = WD_ALIGN_PARAGRAPH.LEFT
            p.paragraph_format.first_line_indent = Inches(0)
            p.paragraph_format.line_spacing = 1.0
            p.paragraph_format.space_after = Pt(0)
            for run in p.runs:
                run.font.name = "Times New Roman"
                run.font.size = Pt(7.0)
                run.font.color.rgb = RGBColor(20, 35, 48)
set_repeat_table_header(table.rows[0])
set_table_borders(table)
doc.add_paragraph().paragraph_format.space_after = Pt(1)

heading("A Farm Management and Weather Modules", 2)
para(
    "The plantation manager stores profile and block data locally and synchronizes records to Firebase under the authenticated user. Reminder records use local preferences, Firebase, and Android AlarmManager scheduling. "
    "The treatment guide is a local library with saved favorites and notes. Weather and geocoding requests use Open-Meteo endpoints; if a weather request fails, the repository returns predefined fallback values to preserve the screen experience. "
    "That fallback supports interface continuity but must be marked as simulated to avoid being mistaken for current conditions."
)
para(
    "The consultation manager currently creates preliminary text using keyword and category rules and persists it locally and remotely. The code assigns a status equivalent to an extension officer having answered, although no human response is produced. "
    "This mismatch is a product-integrity issue: the interface must distinguish automated suggestions from reviewed professional advice and provide a clear escalation path before public release."
)

heading("B Data Handling and Security", 2)
para(
    "The scan history record contains a class label, confidence, timestamp, and a Base64-encoded JPEG. Records are placed below the current Firebase UID; this provides logical path separation but does not by itself prove access-control correctness. "
    "Database security rules, consent text, retention behavior, image-deletion propagation, and third-party inference data handling were not included in the source files examined. "
    "The inference credential is embedded in the Android client source. A credential shipped in a client package can be extracted; the value must be rotated and moved behind a server-side service or appropriately restricted token before a public release."
)

heading("C Interaction, Accessibility and Failure States", 2)
para(
    "Image screening is a multi-step interaction in which capture guidance, waiting feedback, the confidence message, and the result explanation jointly shape a user's interpretation. The current implementation gives a numeric score, but the score should not be described as the probability that a diagnosis is correct unless calibration has been established. The interface should explain that the output is a screening suggestion, show the image used for inference, and make recapture or expert review available when the image is unclear or the class is uncertain. This framing matters because leaf symptoms overlap and environmental images differ from curated training examples."
)
para(
    "Accessibility and legibility are part of workflow reliability. Color should reinforce, rather than carry alone, status information: warning, success, and uncertain states need text and icon labels as well. Touch targets, contrast, scalable text, focus order, and localization should be checked on low-cost handsets and under outdoor glare. The repository review did not measure these properties; they remain product requirements for the next release."
)
para(
    "Failure states include denied camera permission, unavailable gallery content, an offline device, endpoint timeout, malformed or empty prediction responses, and a database write failure after a prediction is shown. These states should preserve user control: provide a retry path, state whether a result was saved, avoid presenting stale weather as current, and prevent a failed upload from being represented as a completed diagnosis. An explicit state machine—idle, image selected, quality warning, uploading, result available, low-confidence abstention, and recoverable error—would make transitions testable."
)

heading("D Data Lifecycle and Separation of Concerns", 2)
para(
    "The history model associates each scan with an authenticated user's database path. A production data contract should distinguish diagnostic metadata from the image itself, since image retention has different privacy, storage, and bandwidth implications. An explicit consent step should describe cloud inference and history storage, while deletion should remove the database record and any retained image copy under the service's control. The source inspection did not establish retention period, access rules, consent language, or deletion behavior; no compliance claim is made here."
)
para(
    "The scan screen coordinates image intake, request execution, response parsing, state updates, mapping, and persistence. Separating those responsibilities behind a typed repository interface would make network behavior substitutable in tests and clarify retry ownership. A versioned response object should validate that the class is recognized and confidence is finite and within range before rendering. This separation could later connect the packaged TensorFlow Lite classifier without changing result presentation or history format."
)

heading("V ARTIFACT-BASED FINDINGS")
para(
    "The source and build artifact support a set of implementation findings, but not a classifier-performance result. The primary end-to-end path is present: a user can choose a leaf image, receive a quality warning, request hosted inference, view a mapped explanation for known labels, and write a user-scoped history record. "
    "The app also contains connected farm workflows for weather, plantation, treatment, reminders, consultation, analytics, and scan comparison. A debug package was built and launched on one handset as a smoke check."
)
para(
    "Three design choices deserve specific interpretation. First, a confidence cutoff can suppress low-score outputs, but without calibration and class-wise validation it does not establish a safe abstention policy. Second, image-quality feedback is non-blocking in the active screen; therefore it may improve user awareness but cannot be treated as an enforced input-quality control. Third, richer farm features increase the potential usefulness of the tool, while also increasing the need for accurate provenance and clear labels for live, cached, synthetic, and automated information."
)
para(
    "The project therefore demonstrates application integration rather than verified diagnostic effectiveness. The remote model's training data and version behavior are not specified in the Android repository, so a result label cannot yet be reproduced from a frozen model artifact. "
    "The static mapping can further create a mismatch if the service's class ontology changes or uses labels absent from the local repository. A versioned model contract, typed response schema, and model/version metadata in each saved record would make the result traceable."
)

heading("VI DISCUSSION")
para(
    "AgriPlus addresses a recurring gap between image classification and practical farm software: a diagnosis screen alone does not organize follow-up, records, reminders, or local context. Its modular feature set provides a plausible path from an individual scan to longitudinal farm management. "
    "The implementation is aligned with coffee-focused mobile work that combines recognition with location or reporting functions [5], while remaining distinct in that the current prediction service is remote and the application repository does not document training or field measurements."
)
para(
    "The most important research implication is that application completeness and model validity are separate dimensions. A polished user flow may reduce interaction friction, but it cannot compensate for domain shift, label ambiguity, or an unvalidated operating threshold. "
    "Conversely, a classifier benchmark alone cannot show whether growers can capture an acceptable image, interpret the output, or complete a useful follow-up task. "
    "Future evaluation should therefore treat the model, the mobile workflow, cloud reliability, and agricultural decision support as linked but separately measurable components."
)
para(
    "Error costs also differ. A false negative can delay inspection, whereas a false positive can cause unnecessary concern or expense. Those costs vary by disorder, severity, and recommended response, so one global accuracy measure would hide important behavior. Evaluation should report class-specific sensitivity and precision and examine whether uncertainty estimates support a useful referral or recapture policy. The interface can communicate a bounded next step, such as inspecting additional leaves or seeking extension advice, without presenting the output as a definitive diagnosis."
)
para(
    "Operational monitoring is needed if the hosted service is maintained after validation. Changes in camera hardware, image preprocessing, model weights, or class definitions can shift output distributions. Release records should identify application and model versions, while a controlled audit sample can reveal whether low-confidence rates, error reports, or class frequencies change materially. Monitoring should not silently collect extra images; any retained sample requires a clear purpose, access controls, and user disclosure."
)
para(
    "The weather advisory is rule-based and uses weather measurements as inputs; its disease-risk text is not a trained epidemiological prediction. It should be communicated as contextual guidance, with its source and update time visible. "
    "Likewise, the treatment library is static and the preliminary consultation response is automated. Content should be reviewed by qualified coffee agronomists, localized to current extension recommendations, and versioned. Without that governance, users may mistake plausible wording for an expert-confirmed prescription."
)

heading("VII LIMITATIONS AND FIELD VALIDATION PLAN")
para(
    "The present study is limited by the absence of an accessible training dataset, test set, model documentation, and field-participant observations. The prediction API can change independently of the Android client. The image-quality thresholds have not been calibrated against expert judgments or downstream classifier performance. "
    "A single build-and-launch smoke check covers packaging and startup only; it does not demonstrate scan success, network failure recovery, data isolation, alarm delivery, usability, or diagnostic correctness."
)
para(
    "A defensible next phase should freeze the endpoint/model version and obtain a coffee-leaf image set with expert-reviewed labels spanning multiple farms, cultivars, seasons, devices, and capture conditions. Splits should be grouped by plant and farm, rather than randomly assigning near-duplicate images, to reduce leakage. "
    "For each class, report sensitivity, precision, F1, macro-averaged measures, confusion matrices, confidence calibration, and abstention coverage at the proposed 0.60 threshold. A locked external farm set should be evaluated once after model selection."
)
para(
    "Reference labels should record disease category and, where feasible, severity, with disagreements adjudicated by qualified plant-health experts. Images should include healthy leaves and common visual confounders, not only obvious positive examples. The sampling plan should document cultivar, farm, date, device, lighting, and acquisition protocol so results can be stratified. A pre-registered analysis plan should define inclusion rules, class mapping, threshold selection, and primary metrics before examination of the locked test set."
)
para(
    "The software study should separately measure image-check false rejection and acceptance, end-to-end inference latency at median and upper percentiles, upload size, network failure behavior, and repeatability across supported devices. "
    "A small formative study with coffee growers and extension professionals should observe image capture, interpretation of uncertainty, ability to retrieve history, and completion of follow-up tasks. Any human-subject protocol should obtain institutional review and informed consent as applicable. "
    "Finally, security review should verify Firebase rules, remove client secrets, document third-party image processing, define retention and deletion, label synthetic fallback data, and replace the simulated extension-officer response with a clearly identified automated aid or a real reviewed workflow."
)
para(
    "The farmer-centered study should assess comprehension as well as task completion. Participants can be asked what the class label and score mean, whether they would act immediately, and when they would seek a second opinion. Findings should guide plain-language explanations and escalation choices, with regional terminology reviewed by extension staff. Weather and treatment content should carry visible source and freshness information; advice involving chemical use should link to current locally approved guidance rather than an unversioned static paragraph."
)
para(
    "The evaluation should predefine separate acceptance criteria for the model and the application. Model criteria might include a minimum class-wise sensitivity on the external set, a maximum expected calibration error, and a stated coverage level when the system abstains. Application criteria might include successful completion of the capture and history workflow under intermittent connectivity, an upper bound for typical request time, and verified deletion of a user's image record. Threshold values should be agreed with agronomy and product stakeholders before testing rather than selected after results are observed."
)
para(
    "A staged release can reduce the consequences of unresolved uncertainty. The first stage can be an internal technical pilot with synthetic or consented examples, followed by a supervised study where extension professionals review outputs, and only later a limited grower trial with clear screening disclaimers and a reporting channel. At each stage, evidence should be reviewed before expanding the audience. If external validation shows a substantial class-specific error or calibration failure, the appropriate response may be to narrow the supported classes, change the abstention threshold, or pause automated suggestions while retaining the farm-record tools."
)
para(
    "For image acquisition, the field protocol should specify how many leaves to photograph, whether both leaf surfaces are relevant, the acceptable distance and framing, and how to avoid glare and heavy shadow. Guidance should be tested with growers who use different Android devices and have varying familiarity with camera settings. The image-quality thresholds can then be assessed against a human-rated sample and against the downstream model's error rate, rather than being treated as universal cutoffs. If the checker rejects images that experts consider usable, or accepts images that routinely produce errors, its thresholds and wording should be revised."
)
para(
    "The study should document whether users can proceed without sharing an image, how cancellation behaves during upload, and what happens when a scan result cannot be written to history. A minimal offline mode could retain non-diagnostic farm records and clearly indicate that classification is unavailable, while avoiding a false impression that a cached result is current. These workflow observations would help determine whether network-dependent screening can be used reliably in the intended areas or whether a separately validated on-device model is a necessary future requirement."
)

heading("VIII CONCLUSION")
para(
    "This paper presented AgriPlus as an Android decision-support prototype that connects coffee-leaf image screening with farm records and operational tools. Source inspection confirms a camera/gallery-to-remote-inference workflow, heuristic quality feedback, confidence-based display control, local disease-text mapping, and Firebase-backed history, alongside weather, plantation, treatment, reminders, analytics, and consultation modules. "
    "It also reveals material boundaries: the active classifier depends on a hosted endpoint, the packaged TensorFlow Lite assets are not wired into the scan path, image-quality feedback is advisory, and the project contains no independent accuracy or field-usability evidence."
)
para(
    "The appropriate conclusion is therefore feasibility of integration, not proof of accurate coffee-disease diagnosis. Expert-labeled multi-farm testing, calibrated uncertainty, secure server-side inference credentials, transparent advice provenance, and farmer-centered evaluation are required before AgriPlus can be described as a validated agricultural diagnostic product."
)

heading("REFERENCES")
references = [
    "[1] S. P. Mohanty, D. P. Hughes, and M. Salathé, “Using deep learning for image-based plant disease detection,” Frontiers in Plant Science, vol. 7, Art. no. 1419, 2016, doi: 10.3389/fpls.2016.01419.",
    "[2] J. Parraga-Alava, K. Cusme, A. Loor, and E. Santander, “RoCoLe: A robusta coffee leaf images dataset for evaluation of machine learning based methods in plant diseases recognition,” Data in Brief, vol. 25, Art. no. 104414, 2019, doi: 10.1016/j.dib.2019.104414.",
    "[3] J. Jepkoech, D. M. Mugo, B. K. Kenduiywo, and E. C. Too, “Arabica coffee leaf images dataset for coffee leaf disease detection and classification,” Data in Brief, vol. 36, Art. no. 107142, 2021, doi: 10.1016/j.dib.2021.107142.",
    "[4] L. M. Tassis, J. E. T. de Souza, and R. A. Krohling, “A deep learning approach combining instance and semantic segmentation to identify diseases and pests of coffee leaves from in-field images,” Computers and Electronics in Agriculture, vol. 186, Art. no. 106191, 2021, doi: 10.1016/j.compag.2021.106191.",
    "[5] E. Hitimana et al., “Revolutionizing coffee farming: A mobile app with GPS-enabled reporting for rapid and accurate on-site detection of coffee leaf diseases using integrated deep learning,” Software, vol. 3, no. 2, pp. 146–168, 2024, doi: 10.3390/software3020007.",
    "[6] K. P. Ferentinos, “Deep learning models for plant disease detection and diagnosis,” Computers and Electronics in Agriculture, vol. 145, pp. 311–318, 2018, doi: 10.1016/j.compag.2018.01.009.",
]
for ref in references:
    p = doc.add_paragraph()
    p.paragraph_format.left_indent = Inches(0.16)
    p.paragraph_format.first_line_indent = Inches(-0.16)
    p.paragraph_format.space_after = Pt(2.2)
    p.paragraph_format.line_spacing = 1.0
    r = p.add_run(ref)
    r.font.size = Pt(7.75)

# End the two-column section explicitly so office renderers balance its final page.
closing = doc.add_section(WD_SECTION.CONTINUOUS)
closing.page_width = Inches(8.27)
closing.page_height = Inches(11.69)
closing.top_margin = Inches(0.62)
closing.bottom_margin = Inches(0.58)
closing.left_margin = Inches(0.66)
closing.right_margin = Inches(0.66)
set_column_layout(closing, count=1)
closing.footer.is_linked_to_previous = True

doc.core_properties.title = "AgriPlus Mobile Decision Support for Coffee Leaf Health Screening and Farm Management"
doc.core_properties.subject = "Design and implementation study of an Android coffee leaf screening prototype"
doc.core_properties.author = ""
doc.core_properties.keywords = "AgriPlus, coffee leaf, mobile decision support, plant disease, Android"
doc.save(OUT_DOCX)
print(OUT_DOCX)
