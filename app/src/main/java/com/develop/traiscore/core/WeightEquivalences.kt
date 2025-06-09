package com.develop.traiscore.core

data class WeightEquivalence(
    val name: String,
    val weightKg: Float,
    val emoji: String,
    val description: String
)

object WeightEquivalences {

    private val equivalences = listOf(
        // Animales
        WeightEquivalence("Un gato", 4f, "🐱", "¡Has levantado el peso de un gato doméstico!"),
        WeightEquivalence("Un perro pequeño", 10f, "🐕", "¡Como cargar un Chihuahua!"),
        WeightEquivalence("Un pavo", 15f, "🦃", "¡Peso de un pavo de Navidad!"),
        WeightEquivalence("Un perro mediano", 25f, "🐕‍🦺", "¡Como un Beagle adulto!"),
        WeightEquivalence("Una oveja", 45f, "🐑", "¡El peso de una oveja lanuda!"),
        WeightEquivalence("Un perro grande", 60f, "🐕", "¡Como un Pastor Alemán!"),
        WeightEquivalence("Una persona promedio", 70f, "🧑", "¡Has levantado tu propio peso!"),
        WeightEquivalence("Un cerdo", 90f, "🐷", "¡Peso de un cerdo bien alimentado!"),
        WeightEquivalence("Un ternero", 120f, "🐄", "¡Como un ternero joven!"),
        WeightEquivalence("Un león", 180f, "🦁", "¡El rey de la selva!"),
        WeightEquivalence("Un oso negro", 200f, "🐻", "¡Fuerza de oso!"),
        WeightEquivalence("Una vaca", 500f, "🐄", "¡Una vaca lechera completa!"),
        WeightEquivalence("Un oso polar", 600f, "🐻‍❄️", "¡Poder ártico!"),
        WeightEquivalence("Un alce", 700f, "🫎", "¡Majestuoso alce canadiense!"),
        WeightEquivalence("Un toro", 800f, "🐂", "¡Fuerza taurina!"),
        WeightEquivalence("Un búfalo", 900f, "🐃", "¡Poder del búfalo!"),
        WeightEquivalence("Un rinoceronte", 1500f, "🦏", "¡Carga de rinoceronte!"),
        WeightEquivalence("Una jirafa", 1800f, "🦒", "¡Alto como una jirafa!"),
        WeightEquivalence("Un hipopótamo", 2500f, "🦛", "¡Bestia del río!"),
        WeightEquivalence("Un elefante", 6000f, "🐘", "¡Memoria de elefante, fuerza de titán!"),
        WeightEquivalence("Una orca", 8000f, "🐋", "¡Gigante de los océanos!"),
        WeightEquivalence("Un T-Rex", 10000f, "🦕", "¡Fuerza prehistórica!"),
        WeightEquivalence("Una ballena azul", 150000f, "🐋", "¡El animal más grande del planeta!"),

        // Vehículos
        WeightEquivalence("Una bicicleta", 15f, "🚴", "¡Listo para el Tour de France!"),
        WeightEquivalence("Una motocicleta", 200f, "🏍️", "¡Rugido de motor!"),
        WeightEquivalence("Un coche pequeño", 1000f, "🚗", "¡Un auto compacto!"),
        WeightEquivalence("Un coche familiar", 1500f, "🚙", "¡SUV familiar!"),
        WeightEquivalence("Una camioneta", 2500f, "🛻", "¡Pick-up resistente!"),
        WeightEquivalence("Un autobús", 12000f, "🚌", "¡Transporte público!"),
        WeightEquivalence("Un camión", 25000f, "🚛", "¡Peso pesado de carretera!"),

        // Objetos cotidianos
        WeightEquivalence("Un iPhone", 0.2f, "📱", "¡Tecnología en tus manos!"),
        WeightEquivalence("Una laptop", 2f, "💻", "¡Potencia portátil!"),
        WeightEquivalence("Una maleta llena", 23f, "🧳", "¡Listo para viajar!"),
        WeightEquivalence("Un televisor grande", 30f, "📺", "¡Entretenimiento pesado!"),
        WeightEquivalence("Una lavadora", 70f, "🧽", "¡Electrodoméstico resistente!"),
        WeightEquivalence("Un refrigerador", 125f, "🧊", "¡Frío que pesa!"),

        // Deportivo/Fitness
        WeightEquivalence("Una mancuerna olímpica", 20f, "🏋️", "¡Entrenamiento serio!"),
        WeightEquivalence("Una barra olímpica", 45f, "🏋️‍♂️", "¡Estándar olímpico!"),
        WeightEquivalence("Un saco de boxeo", 50f, "🥊", "¡Entrenamiento de campeón!"),

        // Comida (divertido)
        WeightEquivalence("1000 hamburguesas", 250f, "🍔", "¡Festival de hamburguesas!"),
        WeightEquivalence("500 pizzas", 1000f, "🍕", "¡Fiesta italiana!"),
        WeightEquivalence("2000 donas", 400f, "🍩", "¡Dulce carga!"),

        // Récords mundiales (aspiracional)
        WeightEquivalence("Récord deadlift amateur", 300f, "🏆", "¡Nivel competitivo!"),
        WeightEquivalence("Récord deadlift profesional", 500f, "🥇", "¡Fuerza de élite!"),
        WeightEquivalence("Récord mundial deadlift", 501f, "👑", "¡Eres una leyenda viviente!")
    )

    /**
     * Encuentra la mejor equivalencia para un peso dado
     * Devuelve la equivalencia más cercana sin exceder el peso
     */
    fun getBestEquivalence(weightKg: Float): WeightEquivalence {
        // Ordenar por peso y encontrar la mejor coincidencia
        val sortedEquivalences = equivalences.sortedBy { it.weightKg }

        return sortedEquivalences
            .lastOrNull { it.weightKg <= weightKg }
            ?: sortedEquivalences.first() // Fallback al más pequeño
    }

    /**
     * Obtiene múltiples equivalencias para mostrar progresión
     */
    fun getProgressionEquivalences(weightKg: Float): List<WeightEquivalence> {
        val sortedEquivalences = equivalences.sortedBy { it.weightKg }
        val current = getBestEquivalence(weightKg)
        val currentIndex = sortedEquivalences.indexOf(current)

        return listOf(
            // Equivalencia actual
            current,
            // Próximas 2-3 metas
            *sortedEquivalences.drop(currentIndex + 1).take(3).toTypedArray()
        )
    }

    /**
     * Calcula cuánto falta para la siguiente meta
     */
    fun getNextGoal(weightKg: Float): WeightEquivalence? {
        val sortedEquivalences = equivalences.sortedBy { it.weightKg }
        return sortedEquivalences.firstOrNull { it.weightKg > weightKg }
    }

    /**
     * Obtiene el progreso hacia la siguiente meta (0.0 - 1.0)
     */
    fun getProgressToNext(weightKg: Float): Float {
        val current = getBestEquivalence(weightKg)
        val next = getNextGoal(weightKg) ?: return 1.0f

        val currentWeight = current.weightKg
        val nextWeight = next.weightKg

        return ((weightKg - currentWeight) / (nextWeight - currentWeight)).coerceIn(0f, 1f)
    }
}