package com.careme.backend.service;

import com.careme.backend.dto.AgentOperation;
import com.careme.backend.entity.ClinicalEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * The simulated assistant, for local development and for the tests that must run
 * without network access.
 *
 * <p>It decides with rules instead of understanding: it is a double for the
 * scaffolding, not an evaluator of the agent. It still asks for the same
 * operations as the real assistant, so the whole path —validation, persistence
 * and the turn's outcome— runs exactly as in operation.
 */
@Service
@ConditionalOnProperty(name = "careme.llm.mode", havingValue = "fake")
public class FakeClinicalAgent implements ClinicalAgent {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Pattern GREETING = Pattern.compile(
            "^(hola|buenas|buenos d[íi]as|qu[ée] tal|adi[óo]s|hasta luego|gracias)\\b");

    /** A request for advice is not a question about what is recorded. */
    private static final Pattern ADVICE_REQUEST = Pattern.compile(
            "recomiend\\w*|\\breceta(r|me)\\b|\\bdeber[íi]a\\b|qu[ée] puedo tomar"
                    + "|\\bdiagn[óo]stica(me|r)?\\b|\\bes grave\\b|qu[ée] me pasa"
                    + "|\\bestoy bien\\b|qu[ée] me conviene");

    /** A request for a general explanation does not depend on the history. */
    private static final Pattern GENERAL_QUESTION = Pattern.compile(
            "(qu[ée]\\s+(es|son|significa)|c[óo]mo funciona|es normal|para qu[ée] sirve)");

    /** Words that tie a message to what the person has recorded. */
    private static final Pattern HISTORY_EVIDENCE = Pattern.compile(
            "\\b(mi|mis|me|m[íi]o|m[íi]os|tengo|tomo|tuve|ten[íi]a|padezco|consta|registr\\w*"
                    + "|historia|diagn[óo]stic\\w*|medic\\w*|presi[óo]n|tensi[óo]n|dolor|s[íi]ntoma\\w*"
                    + "|vacun\\w*|alergia\\w*|a[ñn]o|a[ñn]os|mes|meses|semana|ayer|hoy|he tenido)\\b");

    private static final Pattern QUESTION = Pattern.compile(
            "^[\\s¿¡]*(qu[ée]|qui[ée]n|cu[áa]ndo|d[óo]nde|c[óo]mo|cu[áa]l|cu[áa]nto)\\b");

    /**
     * The words that name a measurement. Word-bounded on purpose: "hipertensión"
     * contains "tensión", and reading a diagnosis as a blood-pressure measurement
     * would file it under the wrong metric.
     */
    private static final Pattern MEASUREMENT_WORDS = Pattern.compile("\\b(presi[óo]n|tensi[óo]n)\\b");

    /** A composite value as it is usually said or written: "145/92" or "145 92". */
    private static final Pattern BLOOD_PRESSURE = Pattern.compile("(\\d{2,3})\\s*[/\\s]\\s*(\\d{2,3})");

    /** A single numeric value, with a dot or a comma as the decimal separator. */
    private static final Pattern SINGLE_VALUE = Pattern.compile("(\\d+(?:[.,]\\d+)?)");

    /** Words too common to help retrieval on their own. */
    private static final List<String> STOP_WORDS = List.of(
            "que", "qué", "cual", "cuál", "como", "cómo", "cuando", "cuándo", "donde", "dónde",
            "tengo", "tiene", "mis", "los", "las", "una", "uno", "para", "por", "con", "del",
            "historia", "clínica", "clinica", "sobre", "algo", "está", "esta");

    private static final String GREETING_REPLY =
            "Hola. Cuéntame un hecho médico y lo registro, o pregúntame por lo que tienes registrado.";

        private static final String PROFILE_INVITATION =
            "Si quieres, podemos completar poco a poco tu perfil personal. Puedes omitirlo y continuar "
                + "con la conversación. ¿Quieres empezar?";

    /**
     * The labels the tracking knows, as the catalogue names them. A question that
     * names one is answered by the measurement channel.
     *
     * <p>Every label is word-bounded on purpose: "hipertensión" contains "tensión",
     * and reading a diagnosis as a blood-pressure question would route it to the wrong
     * channel.
     */
    private static final List<Map.Entry<Pattern, String>> METRIC_LABELS = List.of(
            metricLabel("peso", "weight"),
            metricLabel("circunferencia abdominal", "waist"),
            metricLabel("circunferencia", "waist"),
            metricLabel("presión arterial", "blood_pressure"),
            metricLabel("presión", "blood_pressure"),
            metricLabel("tensión", "blood_pressure"),
            metricLabel("colesterol", "cholesterol"),
            metricLabel("creatinina", "creatinine"),
            metricLabel("glucosa", "fasting_glucose"),
            metricLabel("triglicéridos", "triglycerides"));

    /** A question about the measurements in general, without naming any of them. */
    private static final Pattern MEASUREMENTS_IN_GENERAL =
            Pattern.compile("\\bmediciones?\\b");

        private static final Pattern PROFILE_EVIDENCE = Pattern.compile(
            "\\b(me llamo|mi nombre|nac[íi]|fecha de nacimiento|soy|trabajo de|mi grupo sangu[íi]neo|mi rh|mido)\\b");
        private static final Pattern PROFILE_REQUEST = Pattern.compile(
            "\\b(completar|retomar|continuar).{0,20}perfil\\b|\\bentrevista del perfil\\b");
        private static final Pattern PROFILE_SKIP = Pattern.compile(
            "^(no|ahora no|más tarde|mas tarde|prefiero no|omitir|saltar)\\b");

    /**
     * An affirmative answer, which only means "resume the interview" when the previous
     * turn offered one. The delimiter is explicit: a word boundary does not count an
     * accented letter as a word character, so "sí," would never match one.
     */
    private static final Pattern AFFIRMATIVE = Pattern.compile(
            "^(sí|si|vale|de acuerdo|claro|quiero)(\\s|,|\\.|;|!|$).*");

    private static Map.Entry<Pattern, String> metricLabel(String label, String metric) {
        return Map.entry(Pattern.compile("\\b" + Pattern.quote(label) + "\\b"), metric);
    }

    private static final String DECLINED =
            "No puedo darte un diagnóstico ni recomendarte un tratamiento: solo registro hechos médicos"
                    + " y respondo sobre lo que tienes registrado. Este asistente no diagnostica ni recomienda"
                    + " tratamiento. Si quieres, puedo explicarte algo en términos generales.";

    private static final String NOTHING_REGISTERED =
            "No he registrado nada. Cuéntame qué te ocurrió y cuándo, y lo guardo en tu historia.";

    /** The message could depend on the history or not; the simulated assistant asks. */
    private static final String UNDETERMINED =
            "No sé si tu pregunta depende de tu historia clínica o es una consulta general."
                    + " ¿Me la puedes concretar?";

    private final AgentOperationExecutor executor;

    public FakeClinicalAgent(AgentOperationExecutor executor) {
        this.executor = executor;
    }

    @Override
    public AgentTurn respond(String message, AgentContext context) {
        String normalized = message.trim().toLowerCase(Locale.ROOT);
        if (GREETING.matcher(normalized).find()) {
            return AgentTurn.completed(
                    hasMissingProfile(context.profile()) ? PROFILE_INVITATION : GREETING_REPLY,
                    List.of());
        }
        if (PROFILE_SKIP.matcher(normalized).find()) {
            return AgentTurn.completed(
                    "De acuerdo. Puedes continuar con la conversación y retomar tu perfil más adelante.",
                    List.of());
        }
        if (PROFILE_REQUEST.matcher(normalized).find()
                || (isAffirmative(normalized) && recentProfileInvitation(context.recentTurns()))) {
            return AgentTurn.completed(profileQuestion(context.profile()), List.of());
        }
        if (ADVICE_REQUEST.matcher(normalized).find()) {
            // A request for advice about a measurement is declined while the registered
            // values are still offered; any other request for advice is declined as
            // general conversation.
            AgentOperationCall aboutMeasurement = measurementQuery(message, normalized, true);
            if (aboutMeasurement != null) {
                return run(aboutMeasurement, context);
            }
            return AgentTurn.completed(DECLINED, List.of());
        }
        boolean asksSomething = QUESTION.matcher(normalized).find() || normalized.endsWith("?");
        if (asksSomething) {
            boolean general = GENERAL_QUESTION.matcher(normalized).find();
            boolean aboutTheHistory = HISTORY_EVIDENCE.matcher(normalized).find();
            if (general && !aboutTheHistory) {
                return AgentTurn.completed(
                        "Te lo explico en términos generales: " + message.trim().replaceAll("[?¿]", "")
                                + ". No aplico esto a tu caso ni sustituye a tu médico.",
                        List.of());
            }
            if (!general && !aboutTheHistory) {
                // The simulated assistant cannot tell which channel the question belongs
                // to, so it asks instead of guessing. The turn reports it as a request the
                // consultation could not be run for, which is the clarification outcome.
                return AgentTurn.completed(
                        UNDETERMINED,
                        List.of(AgentOperationResult.rejected(
                                AgentOperationResult.Rejection.NOT_ADMISSIBLE, UNDETERMINED)));
            }
            AgentOperationCall aboutAMeasurement = measurementQuery(message, normalized, false);
            if (aboutAMeasurement != null) {
                return run(aboutAMeasurement, context);
            }
            return run(consultation(message, normalized, context.referenceDate()), context);
        }
        if (normalized.length() < 12) {
            return AgentTurn.completed(NOTHING_REGISTERED, List.of());
        }
        AgentOperationCall profile = profileUpdate(message, normalized);
        if (profile != null) {
            return run(profile, context);
        }
        // A message that mentions a measurement goes through the measurement channel:
        // it is never collected as a clinical fact.
        AgentOperationCall measurement = measurement(message, normalized, context.referenceDate());
        if (measurement != null) {
            return run(measurement, context);
        }
        return run(registration(message, normalized, context.referenceDate()), context);
    }

    private AgentTurn run(AgentOperationCall call, AgentContext context) {
        AgentOperationResult result =
                executor.execute(context.conversationId(), call, context.referenceDate());
        if (result.kind() == AgentOperationResult.Kind.FAILED) {
            return AgentTurn.failed(result.message(), List.of(result));
        }
        return AgentTurn.completed(
                result.message() == null ? NOTHING_REGISTERED : result.message(), List.of(result));
    }

    private static boolean hasMissingProfile(com.careme.backend.entity.PatientProfile profile) {
        return profile == null || profile.name() == null || profile.dateOfBirth() == null
                || profile.sex() == null || profile.occupation() == null || profile.bloodGroup() == null
                || profile.rh() == null || profile.heightCm() == null;
    }

    private static boolean isAffirmative(String normalized) {
        return AFFIRMATIVE.matcher(normalized).matches();
    }

    private static boolean recentProfileInvitation(List<String> recentTurns) {
        return recentTurns.stream().anyMatch(turn -> turn.toLowerCase(Locale.ROOT).contains("perfil"));
    }

    private static String profileQuestion(com.careme.backend.entity.PatientProfile profile) {
        if (profile == null || profile.name() == null) {
            return "Para empezar, ¿cómo te llamas?";
        }
        if (profile.dateOfBirth() == null) {
            return "¿Cuál es tu fecha de nacimiento? Puedes omitirla si no la conoces.";
        }
        if (profile.sex() == null) {
            return "¿Quieres indicar tu sexo? Puedes omitir este dato.";
        }
        if (profile.occupation() == null) {
            return "¿Cuál es tu ocupación? Puedes omitir este dato.";
        }
        if (profile.bloodGroup() == null || profile.rh() == null) {
            return "¿Conoces tu grupo sanguíneo y factor Rh? Puedes omitirlos.";
        }
        return "¿Cuál es tu estatura en centímetros? Puedes omitirla.";
    }

    /**
     * A question that names a metric of the tracking is answered by the measurement
     * channel, with the metrics it names. A question that names none carries no
     * metrics, so the tracking asks which one instead of assuming it.
     */
    private static AgentOperationCall measurementQuery(
            String message, String normalized, boolean interpretationRequested) {
        List<String> metrics = metricsNamed(normalized);
        // A request for advice only reaches the tracking when it names a metric: a request
        // for advice about anything else is declined as conversation. A question reaches it
        // when it names a metric or asks about the measurements in general, in which case the
        // tracking asks which metric instead of assuming one.
        boolean belongsToTheTracking = interpretationRequested
                ? !metrics.isEmpty()
                : !metrics.isEmpty() || MEASUREMENTS_IN_GENERAL.matcher(normalized).find();
        if (!belongsToTheTracking) {
            return null;
        }
        ObjectNode arguments = OBJECT_MAPPER.createObjectNode();
        arguments.put("question", message.trim());
        ArrayNode named = arguments.putArray("metrics");
        metrics.forEach(named::add);
        if (interpretationRequested) {
            arguments.put("interpretation_requested", true);
        }
        return new AgentOperationCall(AgentOperation.CONSULT_MEASUREMENTS, arguments);
    }

    /** The metric codes the message names, read from the labels the tracking uses. */
    private static List<String> metricsNamed(String normalized) {
        return METRIC_LABELS.stream()
                .filter(entry -> entry.getKey().matcher(normalized).find())
                .map(Map.Entry::getValue)
                .distinct()
                .toList();
    }

    private static AgentOperationCall consultation(String message, String normalized, LocalDate referenceDate) {        ObjectNode arguments = OBJECT_MAPPER.createObjectNode();
        arguments.put("question", message.trim());
        ArrayNode terms = arguments.putArray("search_terms");
        termsOf(normalized).forEach(terms::add);
        ClinicalEvent.ClinicalEventType type = typeOf(normalized);
        if (type != null) {
            arguments.put("type", type.name().toLowerCase(Locale.ROOT));
        }
        LocalDate date = dateOf(normalized, referenceDate);
        if (date != null) {
            arguments.put("date_from", date.toString());
        }
        return new AgentOperationCall(AgentOperation.CONSULT_HISTORY, arguments);
    }

    private static AgentOperationCall registration(String message, String normalized, LocalDate referenceDate) {
        ObjectNode arguments = OBJECT_MAPPER.createObjectNode();
        ClinicalEvent.ClinicalEventType type = typeOf(normalized);
        arguments.put("type", (type == null ? ClinicalEvent.ClinicalEventType.NOTE : type)
                .name().toLowerCase(Locale.ROOT));
        arguments.put("content", message.trim());
        LocalDate date = dateOf(normalized, referenceDate);
        if (date != null) {
            arguments.put("date", date.toString());
            arguments.put("date_precision", ClinicalEvent.DatePrecision.EXACT.name().toLowerCase(Locale.ROOT));
            arguments.put("date_text", normalized.contains("ayer") ? "ayer" : "hoy");
        } else if (normalized.contains("hace")) {
            arguments.put("date_precision", ClinicalEvent.DatePrecision.APPROXIMATE.name().toLowerCase(Locale.ROOT));
            arguments.put("date_text", "hace");
        } else {
            arguments.put("date_precision", ClinicalEvent.DatePrecision.UNKNOWN.name().toLowerCase(Locale.ROOT));
        }
        return new AgentOperationCall(AgentOperation.RECORD_NOTE, arguments);
    }

    private static AgentOperationCall profileUpdate(String message, String normalized) {
        if (!PROFILE_EVIDENCE.matcher(normalized).find()) {
            return null;
        }
        ObjectNode arguments = OBJECT_MAPPER.createObjectNode();
        Matcher name = Pattern.compile("(?:me llamo|mi nombre es)\\s+(.+?)[.!?]?$", Pattern.CASE_INSENSITIVE)
                .matcher(message.trim());
        if (name.find()) {
            arguments.put("name", name.group(1).trim());
        }
        Matcher birth = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})").matcher(normalized);
        if (birth.find() && normalized.contains("nac")) {
            arguments.put("date_of_birth", birth.group(1));
        }
        Matcher height = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*(?:cm|cent[íi]metros?)").matcher(normalized);
        if (height.find() && normalized.contains("mido")) {
            arguments.put("height_cm", height.group(1).replace(',', '.'));
        }
        if (normalized.contains("grupo sanguíneo") || normalized.contains("grupo sanguineo")) {
            Matcher group = Pattern.compile("\\b(a|b|ab|o)\\s*(positivo|negativo|[+-])\\b").matcher(normalized);
            if (group.find()) {
                arguments.put("blood_group", group.group(1).toUpperCase(Locale.ROOT));
                arguments.put("rh", group.group(2));
            }
        }
        arguments.put("confirmed", true);
        return arguments.size() == 1 ? null : new AgentOperationCall(AgentOperation.RECORD_PROFILE, arguments);
    }

    /**
     * The words worth searching with. At least one is always produced, because a
     * consultation with neither terms nor filters is not admissible.
     */
    private static List<String> termsOf(String normalized) {
        List<String> terms = Arrays.stream(normalized.split("[^\\p{L}\\p{N}]+"))
                .filter(word -> word.length() > 3 && !STOP_WORDS.contains(word))
                .distinct()
                .limit(4)
                .toList();
        if (!terms.isEmpty()) {
            return terms;
        }
        return Arrays.stream(normalized.split("[^\\p{L}\\p{N}]+"))
                .filter(word -> !word.isEmpty())
                .findFirst()
                .map(List::of)
                .orElseGet(() -> List.of("historia"));
    }

    /**
     * The measurement the message mentions, or {@code null} when it mentions none that
     * the simulated assistant can name with a value.
     */
    private static AgentOperationCall measurement(String message, String normalized, LocalDate referenceDate) {
        String metric = metricOf(normalized);
        if (metric == null) {
            return null;
        }

        ObjectNode arguments = OBJECT_MAPPER.createObjectNode();
        arguments.put("metric", metric);
        ObjectNode values = arguments.putObject("values");
        if ("blood_pressure".equals(metric)) {
            Matcher matcher = BLOOD_PRESSURE.matcher(normalized);
            if (!matcher.find()) {
                return null;
            }
            values.put("systolic", new BigDecimal(matcher.group(1)));
            values.put("diastolic", new BigDecimal(matcher.group(2)));
        } else {
            Matcher matcher = SINGLE_VALUE.matcher(normalized);
            if (!matcher.find()) {
                return null;
            }
            values.put("value", new BigDecimal(matcher.group(1).replace(',', '.')));
        }

        LocalDate date = dateOf(normalized, referenceDate);
        if (date != null) {
            arguments.put("date", date.toString());
            arguments.put("date_text", normalized.contains("ayer") ? "ayer" : "hoy");
        }
        String unit = unitOf(normalized);
        if (unit != null) {
            arguments.put("unit", unit);
        }
        return new AgentOperationCall(AgentOperation.RECORD_MEASUREMENT, arguments);
    }

    /** The metric the message names, or {@code null} when it names none. */
    private static String metricOf(String normalized) {
        if (MEASUREMENT_WORDS.matcher(normalized).find()) {
            return "blood_pressure";
        }
        if (normalized.contains("cintura") || normalized.contains("circunferencia")) {
            return "waist";
        }
        if (normalized.contains("peso") || normalized.contains("pesa")) {
            return "weight";
        }
        return null;
    }

    /** The unit the message names, or {@code null} when it names none. */
    private static String unitOf(String normalized) {
        if (normalized.contains("libras") || normalized.contains(" libra") || normalized.contains("lb")) {
            return "lb";
        }
        if (normalized.contains("pulgadas")) {
            return "in";
        }
        if (normalized.contains("mmhg")) {
            return "mmHg";
        }
        return null;
    }

    private static ClinicalEvent.ClinicalEventType typeOf(String normalized) {
        if (normalized.contains("medic")) {
            return ClinicalEvent.ClinicalEventType.MEDICATION;
        }
        if (normalized.contains("diagnostic")) {
            return ClinicalEvent.ClinicalEventType.DIAGNOSIS;
        }
        // A measurement is no longer a clinical fact, so it never resolves to a type.
        return null;
    }

    /** Only "hoy" and "ayer" resolve to a date the simulated assistant can name. */
    private static LocalDate dateOf(String normalized, LocalDate referenceDate) {
        if (normalized.contains("hoy")) {
            return referenceDate;
        }
        if (normalized.contains("ayer")) {
            return referenceDate.minusDays(1);
        }
        return null;
    }
}
