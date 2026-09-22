/**
 * User-facing copy.
 *
 * Code stays in English; only these values are shown in Spanish. Keeping them
 * in one place avoids scattered literals and makes a future i18n integration a
 * local change.
 */
export const STRINGS = {
    app: {
        title: "Seguimiento corporal",
        description: "Métricas registradas día a día.",
    },
    errors: {
        title: "No se pudieron cargar las mediciones",
        description:
            "No pudimos conectar con el servidor de datos. Comprueba que el backend esté disponible e inténtalo de nuevo.",
        retry: "Reintentar",
    },
    metrics: {
        weightLabel: "Peso",
        weightUnit: "kg",
        waistLabel: "Circunferencia abdominal",
        waistUnit: "cm",
    },
    dashboard: {
        registeredCount: (count: number) => `${count} registros`,
        dateRange: (from: string, to: string) => `Del ${from} al ${to}`,
        empty: "Todavía no hay mediciones registradas.",
        metricSelector: "Métricas visibles",
        loadingMetric: (label: string) => `Cargando ${label}…`,
        metricError: (label: string) => `No se pudo cargar ${label}.`,
        retryMetric: "Reintentar métrica",
        noSelection: "Selecciona al menos una métrica para ver el seguimiento.",
    },
    import: {
        title: "Cargar mediciones desde un archivo",
        description:
            "Sube un archivo CSV con las columnas date, weight_kg y abdominal_circumference_cm. Si un día ya tenía registro, se reemplaza.",
        file: "Archivo CSV",
        selected: (name: string) => `Archivo elegido: ${name}`,
        preview: "Previsualizar",
        previewing: "Revisando el archivo…",
        confirm: "Confirmar carga",
        importing: "Cargando las mediciones…",
        discard: "Descartar",
        previewSummary: (
            total: number,
            created: number,
            replaced: number,
            ignored: number
        ) =>
            `El archivo tiene ${total} filas. Mediciones nuevas: ${created}. Reemplazos de un registro existente: ${replaced}. Filas sin datos: ${ignored}.`,
        previewRowsTitle: "Mediciones que se cargarían",
        previewRowsCaption:
            "Peso y circunferencia abdominal por día, con el efecto que tendría la carga.",
        previewTruncated: (shown: number, total: number) =>
            `Se muestran las primeras ${shown} de ${total} filas.`,
        effectNew: "Nueva",
        effectReplaced: "Reemplaza",
        tableDate: "Fecha",
        tableWeight: "Peso (kg)",
        tableWaist: "Circunferencia (cm)",
        tableEffect: "Efecto",
        noRows: "El archivo no contiene ninguna medición que cargar.",
        fileTooLarge: "El archivo supera los 10 MB. Divídelo en archivos más pequeños.",
        result: (created: number, replaced: number, ignored: number) =>
            `Carga completada. Mediciones nuevas: ${created}. Reemplazos: ${replaced}. Filas sin datos: ${ignored}. El seguimiento ya está actualizado.`,
        rowIssue: (line: number, field: string, reason: string) =>
            `Fila ${line}: ${field} — ${reason}.`,
        errors: {
            header: "No se cargó ninguna medición.",
            invalidStructure:
                "El archivo no tiene la estructura esperada. Debe incluir exactamente las columnas date, weight_kg y abdominal_circumference_cm, con la fecha en formato AAAA-MM-DD.",
            invalidValues: (count: number) => `Motivos encontrados: ${count}.`,
            tooLarge:
                "El archivo supera el límite admitido de 10 MB o 10.000 filas. Divídelo en archivos más pequeños.",
            empty: "El archivo está vacío. Elige un archivo con mediciones.",
            unreadable: "No pudimos leer el archivo. Inténtalo de nuevo.",
            unknown: "No pudimos cargar el archivo. Inténtalo de nuevo.",
        },
        reasons: {
            MISSING_DATE: "falta la fecha",
            INVALID_DATE: "la fecha no es válida",
            FUTURE_DATE: "la fecha no puede ser futura",
            MISSING_VALUE: "falta el valor",
            NOT_A_NUMBER: "no es un número",
            NOT_POSITIVE: "debe ser mayor que cero",
            TOO_MANY_DECIMALS: "usa como máximo un decimal",
            TOO_LARGE: "supera el límite permitido",
            unknown: "no es válido",
        },
    },
    form: {
        title: "Registrar medición",
        description:
            "Anota tu peso y tu circunferencia abdominal de un día. Si ese día ya tenía registro, se reemplaza.",
        date: "Fecha",
        weight: "Peso (kg)",
        waist: "Circunferencia abdominal (cm)",
        submit: "Guardar registro",
        submitting: "Guardando…",
        success:
            "El registro quedó guardado y el seguimiento ya está actualizado.",
        saveFailed:
            "No pudimos guardar la medición. Revisa tu conexión e inténtalo de nuevo; no pierdes lo que escribiste.",
        validation: {
            required: "Completa este campo.",
            notNumber: "Escribe un número válido.",
            notPositive: "El valor debe ser mayor que cero.",
            oneDecimal: "Usa como máximo un decimal.",
            tooLarge: (max: number) => `El valor no puede superar ${max}.`,
            invalidDate: "Elige una fecha válida.",
            futureDate: "La fecha no puede ser futura.",
        },
    },
    charts: {
        weightDescription: "Evolución del peso durante el período registrado.",
        waistDescription:
            "Evolución de la circunferencia abdominal durante el período registrado.",
        summary: (
            label: string,
            unit: string,
            count: number,
            firstValue: string,
            lastValue: string
        ) =>
            `${label}: ${count} mediciones, de ${firstValue} ${unit} a ${lastValue} ${unit}.`,
        keyboardHint:
            "Con la gráfica enfocada, usa las flechas izquierda y derecha para recorrer los valores.",
        trackingSummary: (label: string, unit: string, count: number) =>
            `${label}: ${count === 1 ? "1 medición" : `${count} mediciones`} en ${unit}.`,
        series: (component: string) =>
            component === "systolic"
                ? "Sistólica"
                : component === "diastolic"
                  ? "Diastólica"
                  : "Valor",
    },
    table: {
        title: "Mediciones diarias",
        caption:
            "Peso y circunferencia abdominal por día, ordenados del más reciente al más antiguo.",
        trackingCaption:
            "Mediciones por día de las métricas seleccionadas, ordenadas de la más antigua a la más reciente.",
        date: "Fecha",
        weight: "Peso (kg)",
        waist: "Circunferencia (cm)",
    },
} as const;
