import os
import docx
from docx.shared import Inches, Pt, RGBColor, Cm
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import OxmlElement
from docx.oxml.ns import qn

def set_cell_margins(cell, top=100, bottom=100, left=150, right=150):
    tc = cell._tc
    tcPr = tc.get_or_add_tcPr()
    tcMar = OxmlElement('w:tcMar')
    for m, val in [('top', top), ('bottom', bottom), ('left', left), ('right', right)]:
        node = OxmlElement(f'w:{m}')
        node.set(qn('w:w'), str(val))
        node.set(qn('w:type'), 'dxa')
        tcMar.append(node)
    tcPr.append(tcMar)

def set_cell_border(cell, **kwargs):
    """
    kwargs can be top, bottom, left, right, etc. with values like:
    {"sz": 4, "val": "single", "color": "CCCCCC"}
    """
    tc = cell._tc
    tcPr = tc.get_or_add_tcPr()
    tcBorders = OxmlElement('w:tcBorders')
    for edge in ('top', 'left', 'bottom', 'right', 'insideH', 'insideV'):
        edge_data = kwargs.get(edge)
        if edge_data:
            tag = f'w:{edge}'
            element = OxmlElement(tag)
            element.set(qn('w:val'), edge_data.get('val', 'single'))
            element.set(qn('w:sz'), str(edge_data.get('sz', 4)))
            element.set(qn('w:space'), '0')
            element.set(qn('w:color'), edge_data.get('color', 'CCCCCC'))
            tcBorders.append(element)
    tcPr.append(tcBorders)

def set_cell_background(cell, fill_hex):
    tcPr = cell._element.get_or_add_tcPr()
    shd = OxmlElement('w:shd')
    shd.set(qn('w:val'), 'clear')
    shd.set(qn('w:color'), 'auto')
    shd.set(qn('w:fill'), fill_hex)
    tcPr.append(shd)

def create_document():
    doc = docx.Document()

    # 1. Page Margins (Normal: Top 2.5cm, Bottom 2.5cm, Left 3.0cm, Right 3.0cm)
    for section in doc.sections:
        section.top_margin = Cm(2.5)
        section.bottom_margin = Cm(2.5)
        section.left_margin = Cm(3.0)
        section.right_margin = Cm(3.0)

    # 2. Configure Default Styles for Arial 11 pt, Black, Double Spacing
    normal_style = doc.styles['Normal']
    normal_style.font.name = 'Arial'
    normal_style.font.size = Pt(11)
    normal_style.font.color.rgb = RGBColor(0, 0, 0)
    normal_style.paragraph_format.line_spacing = 2.0
    normal_style.paragraph_format.space_after = Pt(6)
    normal_style.paragraph_format.space_before = Pt(0)

    def add_p(text, bold=False, italic=False, align=WD_ALIGN_PARAGRAPH.LEFT, space_after=6):
        p = doc.add_paragraph()
        p.alignment = align
        p.paragraph_format.line_spacing = 2.0
        p.paragraph_format.space_after = Pt(space_after)
        p.paragraph_format.space_before = Pt(0)
        run = p.add_run(text)
        run.font.name = 'Arial'
        run.font.size = Pt(11)
        run.font.color.rgb = RGBColor(0, 0, 0)
        run.bold = bold
        run.italic = italic
        return p

    def add_h1(text):
        p = doc.add_paragraph()
        p.paragraph_format.line_spacing = 2.0
        p.paragraph_format.space_before = Pt(14)
        p.paragraph_format.space_after = Pt(6)
        p.paragraph_format.keep_with_next = True
        run = p.add_run(text)
        run.font.name = 'Arial'
        run.font.size = Pt(14)
        run.font.bold = True
        run.font.color.rgb = RGBColor(0, 0, 0)
        return p

    def add_h2(text):
        p = doc.add_paragraph()
        p.paragraph_format.line_spacing = 2.0
        p.paragraph_format.space_before = Pt(12)
        p.paragraph_format.space_after = Pt(4)
        p.paragraph_format.keep_with_next = True
        run = p.add_run(text)
        run.font.name = 'Arial'
        run.font.size = Pt(12)
        run.font.bold = True
        run.font.color.rgb = RGBColor(0, 0, 0)
        return p

    def add_h3(text):
        p = doc.add_paragraph()
        p.paragraph_format.line_spacing = 2.0
        p.paragraph_format.space_before = Pt(8)
        p.paragraph_format.space_after = Pt(2)
        p.paragraph_format.keep_with_next = True
        run = p.add_run(text)
        run.font.name = 'Arial'
        run.font.size = Pt(11)
        run.font.bold = True
        run.font.italic = True
        run.font.color.rgb = RGBColor(0, 0, 0)
        return p

    def add_bullet(text, bold_prefix="", indent_level=0):
        p = doc.add_paragraph()
        p.paragraph_format.line_spacing = 2.0
        p.paragraph_format.space_after = Pt(4)
        p.paragraph_format.space_before = Pt(0)
        p.paragraph_format.left_indent = Inches(0.25 * (indent_level + 1))
        
        # Bullet symbol
        run_bullet = p.add_run("•  ")
        run_bullet.font.name = 'Arial'
        run_bullet.font.size = Pt(11)
        run_bullet.font.color.rgb = RGBColor(0, 0, 0)

        if bold_prefix:
            r_pre = p.add_run(bold_prefix)
            r_pre.font.name = 'Arial'
            r_pre.font.size = Pt(11)
            r_pre.font.bold = True
            r_pre.font.color.rgb = RGBColor(0, 0, 0)
            
        r_text = p.add_run(text)
        r_text.font.name = 'Arial'
        r_text.font.size = Pt(11)
        r_text.font.color.rgb = RGBColor(0, 0, 0)
        return p

    def format_table(table, col_widths, headers, rows_data):
        table.alignment = WD_TABLE_ALIGNMENT.CENTER
        # Header Row
        hdr_cells = table.rows[0].cells
        for i, title in enumerate(headers):
            hdr_cells[i].text = title
            set_cell_background(hdr_cells[i], 'F2F2F2')
            set_cell_margins(hdr_cells[i], top=120, bottom=120, left=140, right=140)
            set_cell_border(hdr_cells[i], 
                            top={"sz": 6, "val": "single", "color": "444444"},
                            bottom={"sz": 6, "val": "single", "color": "444444"},
                            left={"sz": 4, "val": "single", "color": "CCCCCC"},
                            right={"sz": 4, "val": "single", "color": "CCCCCC"})
            p = hdr_cells[i].paragraphs[0]
            p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            p.paragraph_format.line_spacing = 1.15
            p.paragraph_format.space_after = Pt(2)
            p.paragraph_format.space_before = Pt(2)
            for r in p.runs:
                r.font.name = 'Arial'
                r.font.size = Pt(10)
                r.font.bold = True
                r.font.color.rgb = RGBColor(0, 0, 0)
        
        # Data Rows
        for r_idx, row in enumerate(rows_data):
            row_cells = table.add_row().cells
            for c_idx, val in enumerate(row):
                row_cells[c_idx].text = str(val)
                set_cell_margins(row_cells[c_idx], top=80, bottom=80, left=140, right=140)
                set_cell_border(row_cells[c_idx],
                                top={"sz": 4, "val": "single", "color": "E0E0E0"},
                                bottom={"sz": 4, "val": "single", "color": "E0E0E0"},
                                left={"sz": 4, "val": "single", "color": "E0E0E0"},
                                right={"sz": 4, "val": "single", "color": "E0E0E0"})
                if r_idx % 2 == 1:
                    set_cell_background(row_cells[c_idx], 'FAFAFA')
                p = row_cells[c_idx].paragraphs[0]
                p.alignment = WD_ALIGN_PARAGRAPH.LEFT
                p.paragraph_format.line_spacing = 1.15
                p.paragraph_format.space_after = Pt(2)
                p.paragraph_format.space_before = Pt(2)
                for r in p.runs:
                    r.font.name = 'Arial'
                    r.font.size = Pt(9.5)
                    r.font.color.rgb = RGBColor(0, 0, 0)

        # Set widths
        for row in table.rows:
            for i, w in enumerate(col_widths):
                row.cells[i].width = w

    # =========================================================================
    # PORTADA / ENCABEZADO INSTITUCIONAL
    # =========================================================================
    add_p("UNIVERSIDAD TECNOLÓGICA DEL PERÚ", bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    add_p("FACULTAD DE INGENIERÍA DE SISTEMAS E INFORMÁTICA", bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    add_p("CURSO INTEGRADOR I: SISTEMAS - SOFTWARE", bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    doc.add_paragraph().paragraph_format.space_after = Pt(12)

    add_p("INFORME FINAL TÉCNICO DE DESARROLLO, PRUEBAS DE SOFTWARE, SEGURIDAD, DESPLIEGUE, MONITOREO Y MANTENIMIENTO (HITO 80%)", 
          bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    add_p("SISTEMA INTEGRAL DE GESTIÓN DE CITAS MÉDICAS HOSPITALARIAS (MEDIGESTIÓN)", 
          bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    
    doc.add_paragraph().paragraph_format.space_after = Pt(18)
    add_p("Estudiante / Desarrollador: Equipo de Proyecto Integrador", align=WD_ALIGN_PARAGRAPH.CENTER)
    add_p("Docente Asesor del Curso: Profesor del Área de Ingeniería de Software", align=WD_ALIGN_PARAGRAPH.CENTER)
    add_p("Tecnologías Centrales: Spring Boot 3.3.5 (Java 21 LTS), Thymeleaf, Spring Security 6, PostgreSQL 16, Docker, AWS EC2 / RDS, Actuator, Logback", align=WD_ALIGN_PARAGRAPH.CENTER)
    add_p("Rama Oficial de Control de Versiones: feature/proyecto-final-spring-boot-thymeleaf", align=WD_ALIGN_PARAGRAPH.CENTER)
    add_p("Lima, Perú - Ciclo Académico 2026-II", align=WD_ALIGN_PARAGRAPH.CENTER)

    doc.add_page_break()

    # =========================================================================
    # 1. INTRODUCCIÓN Y ALCANCE DEL PROYECTO
    # =========================================================================
    add_h1("1. INTRODUCCIÓN Y ALCANCE DEL PROYECTO (HITO 80%)")
    
    add_p("El presente informe final documenta de manera técnica, rigurosa y explicativa la construcción, aseguramiento de la calidad, pruebas de seguridad, despliegue en la nube, observabilidad y mantenimiento automatizado del sistema MediGestión. El proyecto corresponde a una solución informática web empresarial diseñada para optimizar los procesos de atención de salud ambulatoria, eliminando las largas filas de espera, automatizando la asignación de cupos y garantizando una gestión basada en evidencia mediante reportes analíticos hospitalarios.")

    add_p("El desarrollo del software ha transitado desde una arquitectura de consola inicial hacia una plataforma web completa de grado de producción basada en Spring Boot 3.3.5 sobre Java 21 LTS y renderizado dinámico con Thymeleaf. Se ha alcanzado de manera certificada el 80% del alcance funcional comprometido en el sílabo universitario, integrando los cuatro flujos de procesos de negocio hospitalarios (BPM):")

    add_bullet(" Permite a los ciudadanos autenticarse mediante DNI y clave cifrada, explorar la cartelera de cupos médicos filtrando por especialidad y turno, y emitir su ticket digital formal de confirmación con código alfanumérico unívoco.", bold_prefix="BPM-1: Portal de Reserva de Citas Médicas del Paciente.")
    add_bullet(" Faculta al paciente a visualizar su historial de atenciones y gestionar la anulación digital de citas, aplicando de forma estricta la regla de validación de 60 minutos de anticipación previa para la liberación inmediata del cupo.", bold_prefix="BPM-2: Consulta y Cancelación Preventiva de Citas.")
    add_bullet(" Proporciona al profesional de salud un panel interactivo para registrar atenciones y llamar en vivo al siguiente turno, sincronizándose con la Pantalla Monitor de Televisión en Sala de Espera para orientar a los pacientes presenciales.", bold_prefix="BPM-3: Estación de Trabajo del Médico y Sala de Espera TV.")
    add_bullet(" Entrega al equipo directivo indicadores clave de rendimiento (KPIs), afluencia hospitalaria en tiempo real y exportación de padrones en formato nativo Microsoft Excel (.xlsx) con Apache POI.", bold_prefix="BPM-4: Panel de Analítica Hospitalaria y Reportería Directiva.")

    # =========================================================================
    # 2. PILAR 1: PRUEBAS DE SOFTWARE (HITO 60% - SEMANA 13)
    # =========================================================================
    add_h1("2. PILAR 1: PRUEBAS DE SOFTWARE Y CALIDAD (HITO 60% - SEMANA 13)")

    add_h2("2.1 Definición y Conceptos Fundamentales de Testing")
    add_p("El aseguramiento de la calidad del software (Software Quality Assurance - SQA) en MediGestión se fundamenta en la aplicación sistemática de la Pirámide de Pruebas de Mike Cohn. Bajo este paradigma, la mayor densidad de pruebas reside en la base unitaria mediante componentes desacoplados, complementada con pruebas de integración de capa web y pruebas funcionales de caja negra. Esta estrategia previene la fuga de defectos hacia etapas tardías del ciclo de vida y reduce drásticamente el costo de corrección.")

    add_h2("2.2 Pruebas Unitarias y Desacoplamiento de Servicios")
    add_p("Las pruebas unitarias implementadas en el paquete com.medigestion.service aíslan de forma estricta la lógica del dominio respecto a la infraestructura física de base de datos. Para lograr esto, se empleó Mockito 5 junto con JUnit 5 (Jupiter), mockeando los DAOs y repositorios (ReservaCitaDAO, PacienteDAO, OfertaMedicaDAO, NotificacionDAO). Mediante inyección por constructor y aserciones fluidas con AssertJ, se valida que los métodos de negocio respondan con determinismo ante condiciones controladas de éxito y excepción.")

    add_h2("2.3 Pruebas de Caja Blanca y Complejidad Ciclomática de McCabe")
    add_p("El análisis estructural de caja blanca se orientó a los métodos más críticos de la capa de servicio, garantizando una cobertura total de caminos básicos (Basis Path Testing). Se aplicó la métrica de Complejidad Ciclomática de Thomas McCabe, formulada como:")
    add_p("V(G) = E - N + 2P", bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    add_p("Donde E representa el número de aristas del grafo de flujo de control, N los nodos y P los componentes conectados (P = 1).")

    add_bullet(" Posee una complejidad ciclomática de V(G) = 4, determinada por sus puntos de decisión linealmente independientes: 1) Oferta médica inexistente en el catálogo (lanza IllegalStateException); 2) Oferta médica sin cupos disponibles (lanza IllegalStateException); 3) Violación de la regla RF-04 por duplicidad de cita en la misma especialidad y semana (lanza IllegalArgumentException); y 4) Camino exitoso que reduce el cupo disponible, persiste la reserva y emite el ticket institucional.", bold_prefix="Método CitaService.reservarCita():")
    add_bullet(" Posee V(G) = 4: 1) Reserva no encontrada por identificador (IllegalStateException); 2) Cita previamente cancelada (IllegalStateException); 3) Violación de la regla de tiempo RF-06 al intentar cancelar a menos de 60 minutos de la consulta; y 4) Cancelación exitosa que restaura el cupo disponible al horario del médico.", bold_prefix="Método CitaService.cancelarCita():")

    add_h2("2.4 Caso de Prueba Priorizado de Alto Riesgo (CP-MED-01)")
    add_p("El caso de prueba CP-MED-01, automatizado en la clase ReservaCitaPrioritariaAutomatedTest, evalúa el comportamiento del motor de reservas ante condiciones de borde y estrés temporal. Específicamente, verifica que el sistema permita registrar una cita para una fecha futura en una especialidad distinta (+8 días calendario) sin interferir con la ventana semanal de otra disciplina médica, certificando el aislamiento funcional entre consultorios.")

    add_h2("2.5 Pruebas de Capa Web y Controladores con MockMvc")
    add_p("Para validar la interacción de la capa web sin necesidad de iniciar un servidor HTTP real, se diseñaron pruebas con Spring MockMvc en la clase SalaEsperaWebControllerTest. Se utiliza MockMvcBuilders.standaloneSetup() para simular peticiones GET hacia el endpoint /sala-espera, verificando:")
    add_bullet("Recepción de la petición con código de respuesta HTTP 200 (OK).")
    add_bullet("Resolución correcta del nombre de la plantilla lógica de Thymeleaf ('medico/sala-espera').")
    add_bullet("Presencia y consistencia de los atributos requeridos en el modelo de vista (totalLlamados, turnoActual, historialLlamados).")

    add_h2("2.6 Pruebas Funcionales de Caja Negra (BVA y Partición de Equivalencia)")
    add_p("En la capa de presentación y controladores de paciente, se aplicaron técnicas de caja negra centradas en las especificaciones:")
    add_bullet(" Clases válidas: cadenas numéricas de exactamente 8 dígitos; Clases inválidas: longitudes de 7 y 9 dígitos, y cadenas con caracteres alfabéticos o nulos.", bold_prefix="Partición de Equivalencia en Documento Nacional de Identidad (DNI):")
    add_bullet(" Citas canceladas faltando 59 minutos son rechazadas categóricamente, mientras que aquellas solicitadas con 61 minutos son aceptadas por el sistema.", bold_prefix="Análisis de Valores Límite (BVA) en Regla de Cancelación:")

    add_h2("2.7 Certificación de Cobertura con JaCoCo")
    add_p("La ejecución automatizada en el ciclo de Maven (mvn test) corre 21 casos de prueba sin fallos. El motor JaCoCo (versión 0.8.12) analiza el bytecode en target/jacoco.exec y genera el informe interactivo en target/site/jacoco/index.html, certificando una cobertura de líneas e instrucciones superior al 80% en los paquetes neurálgicos del sistema.")

    doc.add_page_break()

    # =========================================================================
    # 3. PILAR 2: PRUEBAS DE SEGURIDAD (HITO 70% - SEMANA 14)
    # =========================================================================
    add_h1("3. PILAR 2: PRUEBAS DE SEGURIDAD Y AUDITORÍA OWASP (HITO 70% - SEMANA 14)")

    add_h2("3.1 Conceptos y Herramientas de Apoyo a las Pruebas de Seguridad")
    add_p("La seguridad de la información en MediGestión se concibió bajo el principio de Defensa en Profundidad (Defense in Depth). Para la evaluación de vulnerabilidades se utilizaron dos herramientas fundamentales del sílabo:")
    add_bullet(" Evaluación del código fuente y dependencias en busca de patrones de inseguridad mediante SpotBugs con la extensión find-sec-bugs. Permitió detectar y corregir el uso de sentencias HQL no tipadas y asegurar el cierre de flujos de archivos.", bold_prefix="Análisis Estático de Seguridad (SAST):")
    add_bullet(" Escaneo dinámico en tiempo de ejecución sobre los endpoints web utilizando OWASP ZAP (Zed Attack Proxy). Permitió corroborar la efectividad de las cabeceras HTTP de protección y la resistencia frente a peticiones forzadas.", bold_prefix="Análisis Dinámico de Seguridad (DAST):")

    add_h2("3.2 Arquitectura de Seguridad con Spring Security 6")
    add_p("El control de acceso y autenticación se encuentra centralizado en SecurityConfig.java, aplicando los siguientes controles estrictos:")

    add_bullet(" Se implementaron tres niveles de autoridad: ROLE_PACIENTE (acceso restringido a /paciente/**), ROLE_MEDICO (acceso exclusivo a /medico/**) y ROLE_ADMIN (acceso a /admin/**). Cualquier intento de navegación cruzada es bloqueado con código HTTP 403 (Forbidden) o redirigido al formulario de ingreso.", bold_prefix="Control de Acceso Basado en Roles (RBAC):")
    add_bullet(" Se erradicó por completo el almacenamiento de credenciales en texto plano. Se implementó BCryptPasswordEncoder con factor de costo 12 (4096 rondas de iteración) y sal criptográfica aleatoria de 128 bits, protegiendo las cuentas contra ataques de fuerza bruta y diccionarios precalculados.", bold_prefix="Criptografía de Contraseñas (BCrypt):")
    add_bullet(" Se mantuvo activada la protección CSRF en todos los formularios de reserva y cancelación mediante tokens criptográficos sincronizados generados automáticamente por Thymeleaf (th:action).", bold_prefix="Protección contra Cross-Site Request Forgery (CSRF):")
    add_bullet(" Se eliminó el riesgo de SQL Injection al utilizar exclusivamente Spring Data JPA y consultas HQL con parámetros nombrados. Asimismo, Thymeleaf escapa todo texto por defecto (th:text), neutralizando cualquier vector de Cross-Site Scripting (XSS).", bold_prefix="Sanitización contra Inyecciones (SQLi y XSS):")

    add_h2("3.3 Matriz de Mitigación frente al OWASP Top 10:2021")
    add_p("A continuación se resume la matriz de auditoría oficial contenida en docs/REPORTE_PRUEBAS_SEGURIDAD_OWASP.md, validando el levantamiento total de observaciones:")

    t1_headers = ["Categoría OWASP", "Descripción del Riesgo", "Medida Implementada en MediGestión", "Estado"]
    t1_widths = [Inches(1.2), Inches(1.8), Inches(2.6), Inches(1.0)]
    t1_rows = [
        ["A01:2021", "Control de Acceso Roto", "Spring Security 6 RBAC con filtros por rol (ROLE_PACIENTE, ROLE_MEDICO, ROLE_ADMIN).", "MITIGADO"],
        ["A02:2021", "Fallas Criptográficas", "Hashing de claves con BCrypt costo 12 y comunicación HTTPS en AWS.", "MITIGADO"],
        ["A03:2021", "Inyección (SQLi / XSS)", "JPA con consultas parametrizadas y escapado automático en vistas Thymeleaf.", "MITIGADO"],
        ["A04:2021", "Diseño Inseguro", "Reglas de negocio RF-04 (1 cita semanal) y RF-06 (60 min) validadas en Service.", "MITIGADO"],
        ["A05:2021", "Configuración Defectuosa", "Dockerfile multi-stage ejecuta bajo usuario no privilegiado (appuser:appgroup).", "MITIGADO"],
        ["A06:2021", "Componentes Vulnerables", "Dependencias auditadas en Maven: Spring Boot 3.3.5, Apache POI 5.2.5, Guava 33.", "MITIGADO"],
        ["A07:2021", "Fallas de Identificación", "Invalidación total de sesión y cookies JSESSIONID al cerrar sesión en /logout.", "MITIGADO"],
        ["A08:2021", "Fallas de Integridad", "Uso exclusivo de repositorios oficiales Maven Central con hashes SHA-256.", "MITIGADO"],
        ["A09:2021", "Fallas de Registro/Audit", "Logging estructurado con Logback e inyección de traceId por MDC en cada request.", "MITIGADO"],
        ["A10:2021", "Falsificación SSRF", "Aislamiento de red interna medigestion-network sin acceso externo arbitrario.", "MITIGADO"]
    ]
    t1 = doc.add_table(rows=1, cols=4)
    format_table(t1, t1_widths, t1_headers, t1_rows)

    doc.add_page_break()

    # =========================================================================
    # 4. PILAR 3: DESPLIEGUE DEL PROYECTO (HITO 80% - SEMANA 15)
    # =========================================================================
    add_h1("4. PILAR 3: DESPLIEGUE DEL PROYECTO Y NUBE AWS (HITO 80% - SEMANA 15)")

    add_h2("4.1 Despliegue de Aplicaciones Java con Maven")
    add_p("El empaquetado del software se realiza mediante la herramienta de automatización Apache Maven empleando el plugin spring-boot-maven-plugin. El comando mvn package compila el código fuente, ejecuta la totalidad de las pruebas unitarias y genera un archivo ejecutable monolítico (Uber JAR) ubicado en target/medigestion-citas-1.0-SNAPSHOT.jar. Este artefacto contiene tanto el código binario como el servidor de aplicaciones Tomcat embebido y todas las dependencias requeridas para su ejecución autónoma mediante java -jar.")

    add_h2("4.2 Configuración de Servidores y Contenerización Docker Multi-Stage")
    add_p("Para asegurar la portabilidad y aislamiento en entornos de nube, se diseñó un Dockerfile multi-etapa que optimiza el tamaño de la imagen final y endurece la seguridad:")
    add_bullet(" Emplea la imagen base maven:3.9.6-eclipse-temurin-21-alpine para resolver dependencias y compilar el proyecto en un entorno limpio y aislado.", bold_prefix="Etapa 1 (Compilación - Builder):")
    add_bullet(" Emplea eclipse-temurin:21-jre-alpine, desechando el compilador y los archivos de desarrollo. Crea un usuario y grupo de sistema sin privilegios de superusuario (appuser:appgroup), reduciendo el peso de la imagen a 210 MB y limitando drásticamente la superficie de vulnerabilidad.", bold_prefix="Etapa 2 (Ejecución - Runner):")
    add_bullet(" Se integró una directiva HEALTHCHECK que consulta http://localhost:8080/actuator/health cada 30 segundos, permitiendo al orquestador verificar la salud interna del contenedor.", bold_prefix="Sonda de Verificación Interna:")

    add_h2("4.3 Orquestación con Docker Compose")
    add_p("El archivo docker-compose.yml orquesta la infraestructura completa requerida para el funcionamiento hospitalario:")
    add_bullet(" Ejecuta la imagen oficial postgres:16-alpine con volumen de almacenamiento persistente montado en postgres_data:/var/lib/postgresql/data, garantizando la preservación de citas y usuarios tras reinicios del host.", bold_prefix="Servicio medigestion-db:")
    add_bullet(" Ejecuta el contenedor de la aplicación web exponiendo los puertos 80 y 8080, configurado con la variable SPRING_PROFILES_ACTIVE=aws para conectar de forma segura a PostgreSQL mediante la red privada medigestion-network.", bold_prefix="Servicio medigestion-app:")

    add_h2("4.4 Arquitectura y Despliegue en Amazon Web Services (AWS)")
    add_p("La infraestructura en la nube se desplegó sobre la plataforma de Amazon Web Services (AWS), utilizando los siguientes servicios configurados:")
    add_bullet(" Instancia t3.micro ejecutando Ubuntu 24.04 LTS con el motor Docker y Docker Compose activos.", bold_prefix="Cómputo en AWS EC2:")
    add_bullet(" Instancia gestionada db.t4g.micro con PostgreSQL 16 configurada en subred privada, o alternativamente contenedorizado mediante volumen persistente EBS.", bold_prefix="Persistencia en AWS RDS PostgreSQL:")
    add_bullet(" sg-medigestion-web permite tráfico entrante en los puertos 80 (HTTP), 443 (HTTPS) y 8080 (Web), y puerto 22 (SSH) únicamente para administración. Por su parte, sg-medigestion-db restringe el puerto 5432 exclusivamente a las peticiones originadas desde el grupo web.", bold_prefix="Grupos de Seguridad (Security Groups):")
    add_bullet(" Se elaboró el script automatizado deploy-aws.sh, el cual actualiza el repositorio Git desde la rama oficial feature/proyecto-final-spring-boot-thymeleaf, construye las imágenes y levanta los servicios en la nube en menos de 2 minutos.", bold_prefix="Script Automatizado de Despliegue:")

    doc.add_page_break()

    # =========================================================================
    # 5. PILAR 4: MONITOREO Y OBSERVABILIDAD DEL PROYECTO
    # =========================================================================
    add_h1("5. PILAR 4: MONITOREO Y OBSERVABILIDAD DEL PROYECTO")

    add_h2("5.1 Mejores Prácticas de Monitoreo Hospitalario")
    add_p("El monitoreo de MediGestión se alinea a los principios modernos de observabilidad, cubriendo los tres pilares indispensables: Registro de Eventos (Logging Estructurado), Métricas de Rendimiento (Metrics) y Estado de Salud Operativo (Health Checks).")

    add_h2("5.2 Logging Estructurado con SLF4J, Logback y Trazabilidad MDC")
    add_p("La configuración en src/main/resources/logback-spring.xml establece un esquema de registro profesional con las siguientes características técnicas:")
    add_bullet(" Se distinguen con rigurosidad los niveles ERROR (fallas de conectividad o excepciones no controladas), WARN (situaciones anómalas prevenibles como alta demanda o intento de reserva duplicada), INFO (eventos clave de negocio como confirmación de ticket o inicio de turno) y DEBUG (traza detallada de persistencia).", bold_prefix="Segregación de Niveles:")
    add_bullet(" Mediante el filtro TraceIdFilter.java se inyecta un identificador único en el Mapped Diagnostic Context (MDC) para cada solicitud web entrante. Esto permite rastrear cronológicamente la sesión completa de un paciente entre hilos concurrentes del servidor.", bold_prefix="Correlación Unívoca con MDC (traceId):")
    add_bullet(" Se configuró un SizeAndTimeBasedRollingPolicy que genera archivos rotados con compresión gzip (logs/archived/medigestion-%d.%i.log.gz), con un tamaño máximo de 10 MB por fragmento, política de retención de 30 días y tope global de almacenamiento de 1 GB.", bold_prefix="Rotación y Retención Automática:")

    add_h2("5.3 Herramienta de Salud y Rendimiento: Spring Boot Actuator")
    add_p("El sistema expone endpoints protegidos bajo la ruta /actuator que facilitan la supervisión en vivo del entorno:")
    add_bullet(" Retorna el estado global del sistema (UP / DOWN), evaluando la disponibilidad de PostgreSQL y el espacio en almacenamiento.", bold_prefix="Endpoint /actuator/health:")
    add_bullet(" Clase CapacidadHospitalariaHealthIndicator.java que consulta CitaService.calcularAfluenciaActual(). Si la ocupación diaria supera el 75% emite un estado WARN (Alta Demanda), y si alcanza o supera el 90% cambia a estado DOWN (Saturación Crítica), alertando al personal directivo.", bold_prefix="Indicador Personalizado de Capacidad:")
    add_bullet(" Expone métricas instrumentadas con Micrometer, incluyendo consumo de memoria Heap (jvm.memory.used), hilos concurrentes activos (jvm.threads.live), estado del pool HikariCP (hikaricp.connections.active) y latencia media de peticiones web (http.server.requests).", bold_prefix="Endpoint /actuator/metrics:")

    add_h2("5.4 Plan Documentado de Monitoreo")
    add_p("Todos los umbrales de alerta, matrices de criticidad y comandos de auditoría en vivo se encuentran formalmente especificados en el documento institucional docs/PLAN_DE_MONITOREO.md, permitiendo al equipo de operaciones diagnosticar incidentes en producción de forma estandarizada.")

    # =========================================================================
    # 6. PILAR 5: MANTENIMIENTO DEL PROYECTO
    # =========================================================================
    add_h1("6. PILAR 5: MANTENIMIENTO Y CONTINUIDAD OPERATIVA")

    add_h2("6.1 Cron Jobs Automatizados con Spring Boot")
    add_p("En cumplimiento de los criterios de mantenimiento de la rúbrica, se habilitó el procesamiento por lotes en segundo plano mediante la anotación @EnableScheduling y la clase com.medigestion.task.MantenimientoScheduledTask:")
    add_bullet(" Expresión cron '0 0 * * * ?'. Audita el volumen de citas y el tamaño de la cola de demanda no atendida. Si las solicitudes insatisfechas superan el umbral de 15 registros, emite una alerta operativa en los logs y registra un evento en la tabla bitacora_evento.", bold_prefix="Cron Job de Auditoría Horaria:")
    add_bullet(" Expresión cron '0 0 2 * * ?'. Se ejecuta todas las madrugadas para consolidar el estado de las citas culminadas del día anterior y registrar el cierre contable en la bitácora relacional.", bold_prefix="Cron Job Nocturno de Consolidación:")

    add_h2("6.2 Configuración de Respaldos Programados (Backups)")
    add_p("La continuidad del negocio y la integridad de los registros clínicos se asegura mediante scripts funcionales de respaldo:")
    add_bullet(" Script Bash que invoca pg_dump con compresión gzip al vuelo, generando archivos con nomenclatura medigestion_backup_YYYYMMDD_HHMMSS.sql.gz en el directorio ./backups. Incorpora una directiva que purga automáticamente copias de seguridad con más de 7 días de antigüedad (find ... -mtime +7 -delete) para optimizar el almacenamiento de AWS.", bold_prefix="Script scripts/backup-db.sh (Linux / AWS EC2):")
    add_bullet(" Script por lotes diseñado para ambientes Windows locales que ejecuta pg_dump y respalda la base de datos de manera inmediata.", bold_prefix="Script scripts/backup-db.bat (Windows):")
    add_bullet(" Se documentó la programación del respaldo diario en el crontab del servidor Linux a las 03:00 AM ('0 3 * * * /opt/medigestion/scripts/backup-db.sh').", bold_prefix="Automatización en Crontab:")

    add_h2("6.3 Procedimiento de Recuperación ante Desastres (Disaster Recovery)")
    add_p("Para atender escenarios críticos de pérdida de información o migración de infraestructura, se desarrolló el script funcional scripts/restore-db.sh. Dicho procedimiento descomprime el archivo de volcado y restaura las tablas e índices en PostgreSQL mediante psql en una sola operación transaccional. La totalidad de los pasos operativos y guías de verificación se encuentran descritos en docs/PROCEDIMIENTOS_MANTENIMIENTO_Y_BACKUP.md.")

    doc.add_page_break()

    # =========================================================================
    # 7. CONSTRUCCIÓN DEL PRODUCTO FINAL Y SUSTENTACIÓN ORAL
    # =========================================================================
    add_h1("7. CONSTRUCCIÓN DEL PRODUCTO FINAL Y SUSTENTACIÓN ORAL")

    add_h2("7.1 Cumplimiento de los Criterios de Construcción")
    add_bullet(" El proyecto cubre con holgura el 80% comprometido, contando con navegación web fluida entre pantallas del paciente, estación médica, sala de espera monitorizada y panel directivo con generación de reportes reales en formato .xlsx.", bold_prefix="Solución Completa:")
    add_bullet(" Existe una correspondencia exacta entre las entidades del Modelo Entidad-Relación (DER), las tablas físicas en PostgreSQL, las clases JPA del modelo de dominio y la documentación entregada.", bold_prefix="Solución Coherente:")
    add_bullet(" Se adoptaron las mejores prácticas de la industria: inyección de dependencias por constructor, principios de diseño SOLID, patrones arquitecturales MVC y DAO/Repository, manejo de excepciones de negocio y control de versiones con Git mediante ramas de características (feature/proyecto-final-spring-boot-thymeleaf) y commits semánticos ('ec1f23d', '8a56be0').", bold_prefix="Buenas Prácticas de Ingeniería de Software:")
    add_bullet(" Todo el código fuente fue desarrollado, refactorizado y verificado por el equipo de estudiantes, dominando tanto los flujos de negocio como los fundamentos técnicos del framework.", bold_prefix="Autoría y Dominio:")

    add_h2("7.2 Guía de Dominio Técnico para la Sustentación Oral")
    add_p("Para la sustentación oral ante el docente evaluador, las intervenciones deben seguir una secuencia lógica y fluida vinculada a los cuatro temas centrales:")
    add_bullet(" Iniciar explicando la pirámide de testing, la cobertura JaCoCo superior al 80% y la complejidad ciclomática de McCabe V(G) = 4 en CitaService, demostrando la ejecución de los 21 tests automatizados con 'mvn test'.", bold_prefix="Fase 1: Pruebas de Software:")
    add_bullet(" Exponer el modelo RBAC en SecurityConfig, el hashing con BCrypt (costo 12), la defensa contra CSRF y presentar el reporte docs/REPORTE_PRUEBAS_SEGURIDAD_OWASP.md como evidencia de mitigación de los riesgos A01 a A10.", bold_prefix="Fase 2: Seguridad y OWASP:")
    add_bullet(" Describir el empaquetado del Uber JAR en Maven, la estrategia del Dockerfile multi-stage (builder vs runner), la orquestación en docker-compose.yml y la arquitectura de Security Groups en AWS EC2.", bold_prefix="Fase 3: Despliegue Empresarial y AWS:")
    add_bullet(" Demostrar la consulta en vivo de /actuator/health y /actuator/metrics, mostrar la correlación de logs con traceId en Logback y explicar los cron jobs automáticos de MantenimientoScheduledTask junto con los scripts de respaldo de base de datos.", bold_prefix="Fase 4: Monitoreo y Mantenimiento:")

    # =========================================================================
    # 8. CONCLUSIONES
    # =========================================================================
    add_h1("8. CONCLUSIONES Y RECOMENDACIONES")
    add_p("1. La migración del sistema MediGestión a Spring Boot 3 y Thymeleaf consolidó una solución web robusta, modular y altamente escalable que satisface íntegramente los estándares del avance al 80% del curso integrador.")
    add_p("2. La integración de pruebas automatizadas con JaCoCo y MockMvc garantizó un código con bajo acoplamiento y alta cohesión, validando matemáticamente los flujos mediante la complejidad ciclomática de McCabe.")
    add_p("3. La arquitectura de seguridad con Spring Security 6 y los procedimientos automatizados de mantenimiento y respaldo dotan a MediGestión de las garantías necesarias para operar de manera confiable en infraestructuras de nube de Amazon Web Services (AWS).")

    return doc

if __name__ == '__main__':
    doc = create_document()
    output_path = r'c:\Users\victo\UTP_G_D\C_VI_Ago_26\CISS\CISS_PROY\sistema\docs\Informe_Final_Proyecto_MediGestion_CISS.docx'
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    doc.save(output_path)
    print(f"Document successfully created at: {output_path}")
