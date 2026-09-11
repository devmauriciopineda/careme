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
        description: "Peso y circunferencia abdominal registrados día a día.",
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
    },
    table: {
        title: "Mediciones diarias",
        caption:
            "Peso y circunferencia abdominal por día, ordenados del más reciente al más antiguo.",
        date: "Fecha",
        weight: "Peso (kg)",
        waist: "Circunferencia (cm)",
    },
} as const;
