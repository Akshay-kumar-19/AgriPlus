package com.example.agriplus.data.treatment

import android.content.Context
import android.content.SharedPreferences

object TreatmentRepository {

    private const val PREFS_NAME = "agriplus_treatment_prefs"
    private const val KEY_FAVORITES = "favorite_guides"
    private const val KEY_NOTE_PREFIX = "note_"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    private val masterGuides: List<TreatmentGuideItem> = listOf(
        TreatmentGuideItem(
            id = "healthy",
            name = "Healthy Coffee Leaf",
            scientificName = "Coffea arabica / canephora",
            category = ConditionCategory.HEALTHY,
            severity = "Optimal Health",
            iconEmoji = "🌿",
            summary = "Vigorous, dark-green glossy foliage with uniform cellular structure and optimal photosynthesis.",
            description = "The foliage displays balanced chlorophyll pigmentation, robust cuticle thickness, and clear interveinal zones. The tree is actively assimilating carbon and supporting balanced berry set.",
            causes = listOf(
                "Optimal balanced soil nutrition (NPK + micronutrients)",
                "Appropriate two-tier shade management (50-60% sunlight)",
                "Adequate soil drainage without root waterlogging",
                "Proper sub-surface soil moisture maintenance"
            ),
            symptoms = listOf(
                "Uniform vibrant green lamina",
                "Firm and glossy leaf surface",
                "No lesions, pustules, chlorosis, or necrotic halos",
                "Healthy branch apical node expansion"
            ),
            treatmentSteps = listOf(
                "Maintain scheduled maintenance routine",
                "Apply post-monsoon general NPK dose (e.g. 17:17:17 or 20:20:20)",
                "Conduct routine sanitary weeding around tree drip zones"
            ),
            preventionTechniques = listOf(
                "Keep two-tier shade trees (Silver Oak / Dadap) thinned before monsoon",
                "Soil testing every 2 seasons to calibrate liming/dolomite requirements",
                "Maintain 3-5 cm leaf mulch around the root zone"
            ),
            fertilizerNutrientGuidance = "Standard maintenance: Apply balanced NPK 60:40:60 kg/ha in 2-3 split applications (Pre-blossom, Post-blossom, Post-monsoon). Supplement with foliar Zinc (0.25%) + Boron (0.1%) during new flush.",
            recommendedChemicals = listOf("No chemical fungicide needed", "Prophylactic bio-stimulants / Seaweed extract"),
            organicAlternatives = listOf("Vermicompost 2-3 kg/plant annually", "Neem cake application 250g/plant")
        ),

        TreatmentGuideItem(
            id = "leaf_rust",
            name = "Coffee Leaf Rust (CLR)",
            scientificName = "Hemileia vastatrix",
            category = ConditionCategory.FUNGAL_DISEASE,
            severity = "Severe Risk",
            iconEmoji = "🍂",
            summary = "The most destructive coffee fungal disease worldwide, producing distinctive orange powdery pustules.",
            description = "Coffee Leaf Rust penetrates through the stomata on the underside of leaves. It causes widespread defoliation, twig dieback, and dramatic loss of berry yield in subsequent crops if left unchecked.",
            causes = listOf(
                "Prolonged leaf wetness (>3-4 hours) with humidity above 80%",
                "Warm ambient temperatures (21°C to 25°C)",
                "Dense overgrown shade hindering internal bush ventilation",
                "Splash dispersal of urediniospores during rain showers"
            ),
            symptoms = listOf(
                "Yellow spots on upper leaf surfaces that enlarge into circular lesions",
                "Bright orange-yellow powdery fungal spore masses beneath the leaf",
                "Premature leaf drop leaving bare twigs",
                "Severe crop loss and subsequent season yield collapse"
            ),
            treatmentSteps = listOf(
                "1. Immediately prune and burn heavily infested defoliated twigs.",
                "2. Apply systemic fungicide: Hexaconazole 5% EC (2 ml/L) or Triadimefon 25% WP (1 g/L).",
                "3. Follow up with protective Copper Oxychloride 50% WP (2.5 g/L) or 0.5% neutral Bordeaux mixture after 15 days.",
                "4. Ensure full spray coverage on the lower leaf surface where stomata reside."
            ),
            preventionTechniques = listOf(
                "Pre-monsoon (May-June) protective 0.5% Bordeaux spray.",
                "Mid-monsoon (August) or post-monsoon (October) systemic/copper spray.",
                "Prune dead wood and suckers to open canopy for sunlight and air.",
                "Plant rust-tolerant varieties like Selection 795, Cauvery, or Chandragiri."
            ),
            fertilizerNutrientGuidance = "Avoid heavy late-season Nitrogen fertilizers when rust is active. Fortify plant vigor with Potassium (MOP) to strengthen cell walls and Silicon-based foliar nutrients.",
            recommendedChemicals = listOf("Hexaconazole 5% EC", "Oxycarboxin 20% EC", "Copper Oxychloride 50% WP", "Mancozeb 75% WP"),
            organicAlternatives = listOf("0.5% Neutral Bordeaux Mixture", "Trichoderma viride foliar wash", "Neem seed kernel extract (5%)")
        ),

        TreatmentGuideItem(
            id = "phoma",
            name = "Phoma Leaf Spot & Dieback",
            scientificName = "Phoma costarricensis",
            category = ConditionCategory.FUNGAL_DISEASE,
            severity = "Moderate to Severe",
            iconEmoji = "🌫️",
            summary = "Causes dark irregular necrotic lesions on leaves and tender shoots in cool, humid, windy conditions.",
            description = "Favored by high elevation estates subjected to strong cool winds and mist. Infection begins at leaf margins and rapidly advances inwards, causing leaf curl, blackening of tips, and severe shoot die-back.",
            causes = listOf(
                "Continuous high humidity accompanied by cool temperatures (15-20°C)",
                "Strong winds creating micro-abrasions on tender leaf tissue",
                "Overhead mist and high elevation cloud cover",
                "Poor soil aeration in water-saturated root zones"
            ),
            symptoms = listOf(
                "Dark brown to black irregular sunken spots with concentric rings",
                "Leaf margin burning and upward curling",
                "Blackening and die-back of young lateral branches",
                "Premature flower and pinhead berry dropping"
            ),
            treatmentSteps = listOf(
                "1. Prune all blackened and dead shoots 2 inches below the infected tissue.",
                "2. Spray Carbendazim 50% WP (1 g/L) or Thiophanate-methyl (1 g/L).",
                "3. Alternate with Mancozeb (2.5 g/L) every 14 days during cloudy spells.",
                "4. Clear weeds around basins to lower humidity around lower branches."
            ),
            preventionTechniques = listOf(
                "Establish effective windbreak trees (Silver Oak / Casuarina) on windward estate edges.",
                "Regulate overhead canopy to eliminate excessive continuous shade.",
                "Avoid applying overhead sprinkler irrigation in late afternoon."
            ),
            fertilizerNutrientGuidance = "Enhance Calcium and Boron foliar levels to thicken young shoot epidermal tissue. Avoid excessive vegetative Nitrogen flushing during peak mist seasons.",
            recommendedChemicals = listOf("Carbendazim 50% WP", "Thiophanate-methyl 70% WP", "Azoxystrobin + Difenoconazole", "Mancozeb 75% WP"),
            organicAlternatives = listOf("Copper hydroxide (2 g/L)", "Bacillus subtilis bio-fungicide", "Garlic extract spray")
        ),

        TreatmentGuideItem(
            id = "mite",
            name = "Red Spider Mite Infestation",
            scientificName = "Oligonychus coffeae",
            category = ConditionCategory.PEST,
            severity = "Moderate",
            iconEmoji = "🐛",
            summary = "Sap-sucking tiny arachnids that cause bronzing and dulling of coffee foliage during dry, warm weather.",
            description = "Mites colony clusters on the upper leaf surface along the main veins, piercing leaf cells and sucking sap. Severely attacked leaves turn dusty bronze or copper-brown, dry up, and drop prematurely.",
            causes = listOf(
                "Prolonged dry spells with high ambient temperatures (>28°C)",
                "Low relative humidity (<50%) and dusty road borders",
                "Indiscriminate use of broad-spectrum synthetic pyrethroids killing natural predators"
            ),
            symptoms = listOf(
                "Rusty bronze or coppery discoloration on upper leaf surface",
                "Fine delicate webbing visible under sunlight",
                "Tiny reddish-brown specks crawling when magnified",
                "Brittle, desiccated foliage and retarded berry maturation"
            ),
            treatmentSteps = listOf(
                "1. Wash infested bushes thoroughly with high-pressure overhead water spray.",
                "2. Spray Wettable Sulfur 80% WP (3 g/L) in early morning hours.",
                "3. For severe infestations, apply Abamectin 1.9% EC (0.5 ml/L) or Propargite 57% EC (2 ml/L).",
                "4. Repeat spray after 7-10 days to target newly hatched nymph mites."
            ),
            preventionTechniques = listOf(
                "Plant boundary hedges along estate dirt roads to filter dust particles.",
                "Conserve natural predator mite populations (Phytoseiidae and ladybird beetles).",
                "Avoid chemical spraying during high noon heat to prevent phytotoxicity."
            ),
            fertilizerNutrientGuidance = "Ensure consistent soil moisture. Potassium-fortified nutrition helps plants withstand suction stress and reduces cell desiccation.",
            recommendedChemicals = listOf("Wettable Sulfur 80% WP", "Abamectin 1.9% EC", "Propargite 57% EC", "Spiromesifen 22.9% SC"),
            organicAlternatives = listOf("Neem oil 0.03% (5 ml/L) + soap emulsifier", "Pongamia (Karanja) oil spray", "Sulfur dust")
        ),

        TreatmentGuideItem(
            id = "nitrogen",
            name = "Nitrogen (N) Deficiency",
            scientificName = "Chlorosis Vegetativa",
            category = ConditionCategory.NUTRIENT_DEFICIENCY,
            severity = "Moderate",
            iconEmoji = "🟡",
            summary = "General uniform pale-yellowing starting from older lower foliage, causing stunting.",
            description = "Nitrogen is highly mobile in coffee plants. When deficient, the plant translocates nitrogen from older leaves to young growing shoot tips, causing generalized yellowing of mature lower leaves.",
            causes = listOf(
                "Low organic matter in soil or depleted fertility",
                "Heavy leaching caused by torrential monsoon rains",
                "Waterlogging asphyxiating root respiration and nutrient uptake",
                "Competition from rampant unmanaged weeds"
            ),
            symptoms = listOf(
                "Older leaves turn pale yellowish-green to bright lemon yellow",
                "Stunted internodes and thin, spindly branch growth",
                "Small leaves with reduced overall photosynthetic area",
                "Early drop of mature leaves and poor berry filling"
            ),
            treatmentSteps = listOf(
                "1. Soil application: Apply Urea (46% N) at 50-75 g/plant or Ammonium Sulfate.",
                "2. Foliar rescue: Spray 1% Urea solution (10 g/L water) for immediate leaf absorption.",
                "3. Incorporate composted farmyard manure (FYM) around the tree drip circle."
            ),
            preventionTechniques = listOf(
                "Split nitrogen applications into 3-4 cycles rather than one heavy dose.",
                "Plant nitrogen-fixing shade trees (Dadap - Erythrina lithosperma).",
                "Maintain mulch cover to prevent erosion and nitrogen runoff."
            ),
            fertilizerNutrientGuidance = "Standard annual dose: 120-160 kg N/ha for high-yield Arabica/Robusta. Pre-monsoon: 40 kg, Post-monsoon: 60 kg, Post-harvest: 40 kg.",
            recommendedChemicals = listOf("Urea (46% N)", "Calcium Ammonium Nitrate (CAN 25% N)", "Ammonium Sulfate (21% N)"),
            organicAlternatives = listOf("Poultry manure 2 kg/plant", "Well-rotted cow manure 5 kg/plant", "Neem cake 300 g/plant")
        ),

        TreatmentGuideItem(
            id = "potassium",
            name = "Potassium (K) Deficiency",
            scientificName = "Marginal Necrosis",
            category = ConditionCategory.NUTRIENT_DEFICIENCY,
            severity = "Moderate to High",
            iconEmoji = "🟣",
            summary = "Marginal scorch and necrotic browning of leaf edges, leading to poor berry bean filling.",
            description = "Potassium regulates water balance, stomatal conductance, and carbohydrate transport into developing coffee cherries. Deficiencies directly translate into lightweight beans (floats) and premature dieback.",
            causes = listOf(
                "Heavy crop load drawing excessive potassium reserves into berries",
                "Highly acidic soils (pH < 5.0) locking up potassium exchange",
                "Sandy soils with low cation exchange capacity (CEC)"
            ),
            symptoms = listOf(
                "Yellowing followed by dark-brown scorched necrotic leaf margins",
                "Burnt leaf appearance progressing inward between veins",
                "Premature dropping of leaves supporting berry clusters",
                "High percentage of hollow, defective, or light coffee cherries"
            ),
            treatmentSteps = listOf(
                "1. Soil apply Muriate of Potash (MOP - 60% K2O) or Potassium Sulfate (SOP) at 60-80 g/plant.",
                "2. Spray 1.5% Potassium Nitrate (KNO3) (15 g/L) foliar spray during berry development.",
                "3. Repeat foliar spray after 15 days for quick absorption."
            ),
            preventionTechniques = listOf(
                "Apply balanced N:K2O ratio (1:1 during vegetative, 1:1.5 during berry expansion).",
                "Recycle coffee pulp and husk compost back into the plantation soil.",
                "Ensure soil pH is maintained between 6.0 and 6.5 through agricultural lime."
            ),
            fertilizerNutrientGuidance = "Apply 120-180 kg K2O/ha annually. Crucial application window is during rapid cherry swelling (August-October in South India/Latin America).",
            recommendedChemicals = listOf("Muriate of Potash (MOP 60% K2O)", "Sulfate of Potash (SOP 50% K2O)", "Potassium Nitrate (13-0-45)"),
            organicAlternatives = listOf("Coffee pulp compost", "Wood ash (sparingly, 100g/plant)", "Banana stalk bio-compost")
        ),

        TreatmentGuideItem(
            id = "phosphorus",
            name = "Phosphorus (P) Deficiency",
            scientificName = "Root & Energy Deficiency",
            category = ConditionCategory.NUTRIENT_DEFICIENCY,
            severity = "Mild to Moderate",
            iconEmoji = "🟢",
            summary = "Abnormally dark green, dull, or purplish bronze leaves with stunted root growth.",
            description = "Phosphorus is fundamental for ATP energy transfer, flower bud differentiation, and deep feeder root development in coffee trees.",
            causes = listOf(
                "Acidic laterite soils fixing phosphorus with Iron and Aluminum ions",
                "Cold soil temperatures slowing phosphorus root absorption",
                "Poor soil organic matter content"
            ),
            symptoms = listOf(
                "Foliage becomes abnormally dark, dull blue-green",
                "Purplish-bronze or reddish pigmentation on older leaves",
                "Poor root growth and failure of young seedlings to establish",
                "Substantially delayed flowering and poor berry set"
            ),
            treatmentSteps = listOf(
                "1. Apply Rock Phosphate or Single Super Phosphate (SSP) deep in soil basin (not surface).",
                "2. Spray 1% DAP (Diammonium Phosphate - 10 g/L) or 0.5% Mono-Potassium Phosphate (0-52-34).",
                "3. Apply mycorrhizal fungi (VAM) inoculant to mobilize fixed soil phosphorus."
            ),
            preventionTechniques = listOf(
                "Apply agricultural lime/dolomite (1-2 tons/ha) every 2-3 years to buffer soil pH.",
                "Incorporate organic compost which releases organic acids to free fixed phosphorus."
            ),
            fertilizerNutrientGuidance = "Apply 40-60 kg P2O5/ha per year. Rock phosphate is best applied during pre-monsoon into the soil root zone.",
            recommendedChemicals = listOf("Single Super Phosphate (16% P2O5)", "Diammonium Phosphate (DAP 46% P2O5)", "Rock Phosphate (20-28% P2O5)"),
            organicAlternatives = listOf("Steamed bone meal 200 g/plant", "Phosphate Rich Organic Manure (PROM)")
        ),

        TreatmentGuideItem(
            id = "iron",
            name = "Iron (Fe) Deficiency",
            scientificName = "Interveinal Chlorosis",
            category = ConditionCategory.NUTRIENT_DEFICIENCY,
            severity = "Moderate",
            iconEmoji = "⚪",
            summary = "Prominent sharp yellowing of new apical leaves with green veins forming a network pattern.",
            description = "Iron is required for chlorophyll synthesis. Because iron is immobile in plant tissue, deficiency symptoms appear strictly on the youngest leaves at shoot tips.",
            causes = listOf(
                "High soil pH or over-liming with calcium",
                "Poor root aeration due to severe soil compaction or waterlogging",
                "Excessive heavy metal concentrations (Zinc/Copper/Manganese)"
            ),
            symptoms = listOf(
                "Young leaves turn ivory yellow or cream colored",
                "Veins remain sharply dark green, creating an intricate mesh pattern",
                "In severe stages, new leaves emerge almost pure white and necrotic",
                "Stunted terminal branch growth"
            ),
            treatmentSteps = listOf(
                "1. Foliar spray: Ferrous Sulfate (FeSO4) 0.5% (5 g/L) + Citric acid (1 g/L).",
                "2. Chelated Iron: Spray or soil drench Fe-EDTA or Fe-EDDHA (1-2 g/L).",
                "3. Aerate compacted soil around root zone."
            ),
            preventionTechniques = listOf(
                "Avoid over-liming; maintain soil pH strictly below 6.8.",
                "Ensure proper drainage in low-lying plantation patches."
            ),
            fertilizerNutrientGuidance = "Foliar application of 0.25-0.5% Ferrous sulfate with wetting agent twice at 15-day intervals during new vegetative flush.",
            recommendedChemicals = listOf("Ferrous Sulfate (19% Fe)", "Chelated Iron Fe-EDDHA 6%", "Fe-EDTA 12%"),
            organicAlternatives = listOf("Humic acid soil drench", "Composted cow manure slurry")
        ),

        TreatmentGuideItem(
            id = "magnesium",
            name = "Magnesium (Mg) Deficiency",
            scientificName = "Interveinal Browning & Chlorosis",
            category = ConditionCategory.NUTRIENT_DEFICIENCY,
            severity = "Moderate",
            iconEmoji = "🟡",
            summary = "Inverted V-shaped yellowing between main veins of older leaves, while veins stay green.",
            description = "Magnesium is the central atom of the chlorophyll molecule. Deficiency severely reduces the tree's photosynthetic capacity, especially under intense sunlight.",
            causes = listOf(
                "Excessive potassium or calcium fertilization competing with magnesium uptake",
                "Leaching in high-rainfall acidic soils",
                "Unshaded coffee bushes exposed to direct noon sun"
            ),
            symptoms = listOf(
                "Interveinal chlorosis creating a green herringbone or inverted V pattern",
                "Yellow zones turn reddish-brown or necrotic over time",
                "Older leaves drop prematurely leaving barren nodes",
                "Poor bean density and uneven cherry ripening"
            ),
            treatmentSteps = listOf(
                "1. Spray Magnesium Sulfate (Epsom Salt) 1% (10 g/L) onto foliage.",
                "2. Soil application: Apply Magnesium Sulfate 25-50 g/plant or Dolomite lime.",
                "3. Repeat foliar spray after 12-14 days."
            ),
            preventionTechniques = listOf(
                "Use Dolomite (Magnesium Limestone) for regular soil pH correction.",
                "Balance Potassium:Magnesium application ratios."
            ),
            fertilizerNutrientGuidance = "Apply 25-40 kg MgO/ha annually. Dolomite application of 500-1000 kg/ha every 2-3 years provides sustained magnesium reserves.",
            recommendedChemicals = listOf("Magnesium Sulfate (Epsom Salt 9.6% Mg)", "Dolomite (10-12% Mg)"),
            organicAlternatives = listOf("Dolomitic limestone", "Kieserite")
        ),

        TreatmentGuideItem(
            id = "calcium",
            name = "Calcium (Ca) Deficiency",
            scientificName = "Tip Burn & Leaf Distortion",
            category = ConditionCategory.NUTRIENT_DEFICIENCY,
            severity = "Moderate",
            iconEmoji = "🟤",
            summary = "Distorted, hook-shaped young leaves with ragged margins and necrotic apical tips.",
            description = "Calcium forms calcium pectate in cell walls. Deficiency weakens structural plant integrity, causing young expanding leaves to warp, cup downward, and tear.",
            causes = listOf(
                "Strongly acidic soils (pH < 4.8)",
                "Prolonged dry spells halting transpiration-driven calcium flow",
                "High soil aluminum toxicity inhibiting root uptake"
            ),
            symptoms = listOf(
                "Young leaves become hooked, cupped, or irregularly shaped",
                "Margins appear scorched, ragged, or chlorotic",
                "Growing tips die back (blunt apical buds)",
                "Root tips become stubby, dark, and gelatinous"
            ),
            treatmentSteps = listOf(
                "1. Spray Calcium Nitrate 0.5% (5 g/L) onto tender foliage.",
                "2. Apply Agricultural Lime (CaCO3) or Dolomite to soil at 200-400 g/plant.",
                "3. Maintain regular irrigation during hot spells to facilitate calcium uptake."
            ),
            preventionTechniques = listOf(
                "Routine soil testing and scheduled liming to keep pH at 6.0-6.5.",
                "Maintain uniform soil moisture with leaf mulch."
            ),
            fertilizerNutrientGuidance = "Soil apply 1-2 tons of Agricultural Lime per hectare based on soil acidity index.",
            recommendedChemicals = listOf("Calcium Nitrate (19% Ca, 15.5% N)", "Agricultural Lime (Calcium Carbonate)", "Gypsum (Calcium Sulfate)"),
            organicAlternatives = listOf("Crushed limestone", "Eggshell compost powder", "Wood ash")
        ),

        TreatmentGuideItem(
            id = "boron",
            name = "Boron (B) Deficiency",
            scientificName = "Apical Distortion & Crinkle Leaf",
            category = ConditionCategory.NUTRIENT_DEFICIENCY,
            severity = "Moderate",
            iconEmoji = "🟠",
            summary = "Crinkled, leathery small leaves with shortened bushy internodes and flower abortion.",
            description = "Boron is essential for cell division, pollen tube germination, and cherry development. Deficiencies lead to extensive blossom drop and star flowers.",
            causes = listOf(
                "High soil leaching in torrential hill estates",
                "Dry spells during flowering and pinhead berry stage",
                "Over-limed soils with pH > 6.8"
            ),
            symptoms = listOf(
                "Young leaves are small, deformed, thick, brittle, and crinkled",
                "Shoot tips die, triggering multiple side shoots (witches' broom look)",
                "High rate of flower abortion (star flowers that fail to fruit)",
                "Heart-shaped or cleft coffee cherries"
            ),
            treatmentSteps = listOf(
                "1. Spray Solubor or Borax at 0.1% (1 g/L water) onto new flush.",
                "2. Apply 10-15 g Borax per plant incorporated into soil basin.",
                "3. Avoid overdosing, as Boron toxicity threshold is narrow."
            ),
            preventionTechniques = listOf(
                "Include Boron in pre-blossom and post-blossom micronutrient foliar sprays.",
                "Maintain organic soil mulch."
            ),
            fertilizerNutrientGuidance = "Apply 1-2 kg actual Boron/ha every 2 years. Spray 0.1% Solubor before flowering and at pinhead stage.",
            recommendedChemicals = listOf("Disodium Octaborate Tetrahydrate (Solubor 20% B)", "Borax (11% B)", "Boric Acid (17% B)"),
            organicAlternatives = listOf("Composted sea kelp", "Boron-fortified organic compost")
        ),

        TreatmentGuideItem(
            id = "manganese",
            name = "Manganese (Mn) Deficiency",
            scientificName = "Interveinal Mottling",
            category = ConditionCategory.NUTRIENT_DEFICIENCY,
            severity = "Mild to Moderate",
            iconEmoji = "🔴",
            summary = "Chequered mosaic pattern of pale green/yellow spots between veins on young-to-mid leaves.",
            description = "Manganese is a key activator in enzymatic and photosynthetic systems. Deficiency leads to distinct speckled chlorosis.",
            causes = listOf(
                "Alkaline or over-limed soils",
                "Excessive soil phosphate levels locking manganese",
                "Sandy soils with low mineral reserves"
            ),
            symptoms = listOf(
                "Pale yellow or whitish mottling between green veins",
                "Fine reticulate vein network remains green",
                "Leaves remain normal in size unlike zinc deficiency",
                "Reduced root growth and lower berry sugar accumulation"
            ),
            treatmentSteps = listOf(
                "1. Foliar spray: Manganese Sulfate 0.3% (3 g/L) + 0.1% lime buffer.",
                "2. Spray chelates (Mn-EDTA 1 g/L) for rapid uptake.",
                "3. Check soil pH and avoid further liming."
            ),
            preventionTechniques = listOf(
                "Incorporate Manganese Sulfate in annual micronutrient blend (Zinc + Manganese + Boron).",
                "Maintain soil pH at 5.8 to 6.3."
            ),
            fertilizerNutrientGuidance = "Foliar spray of 2-3 g/L Manganese Sulfate during active flush (May and October).",
            recommendedChemicals = listOf("Manganese Sulfate (30% Mn)", "Chelated Mn-EDTA (12% Mn)"),
            organicAlternatives = listOf("Composted forest leaf litter", "Rock mineral dust")
        )
    )

    fun getAllGuides(): List<TreatmentGuideItem> {
        val favs = getFavoriteIds()
        return masterGuides.map { guide ->
            guide.copy(
                isFavorite = favs.contains(guide.id),
                customUserNote = getUserNote(guide.id)
            )
        }
    }

    fun getGuideById(id: String): TreatmentGuideItem? {
        val cleanId = id.lowercase().trim()
        val guide = masterGuides.find {
            it.id == cleanId || cleanId.contains(it.id) || it.name.lowercase().contains(cleanId)
        } ?: masterGuides.firstOrNull()

        return guide?.let {
            it.copy(
                isFavorite = isFavorite(it.id),
                customUserNote = getUserNote(it.id)
            )
        }
    }

    fun searchGuides(query: String, category: ConditionCategory): List<TreatmentGuideItem> {
        val all = getAllGuides()
        val q = query.trim().lowercase()

        return all.filter { item ->
            val matchesCategory = when (category) {
                ConditionCategory.ALL -> true
                ConditionCategory.FAVORITES -> item.isFavorite
                else -> item.category == category
            }

            val matchesQuery = if (q.isEmpty()) true else {
                item.name.lowercase().contains(q) ||
                        item.scientificName.lowercase().contains(q) ||
                        item.summary.lowercase().contains(q) ||
                        item.causes.any { it.lowercase().contains(q) } ||
                        item.symptoms.any { it.lowercase().contains(q) } ||
                        item.treatmentSteps.any { it.lowercase().contains(q) }
            }

            matchesCategory && matchesQuery
        }
    }

    fun toggleFavorite(id: String): Boolean {
        val favs = getFavoriteIds().toMutableSet()
        val isNowFav = if (favs.contains(id)) {
            favs.remove(id)
            false
        } else {
            favs.add(id)
            true
        }
        prefs?.edit()?.putStringSet(KEY_FAVORITES, favs)?.apply()
        return isNowFav
    }

    fun isFavorite(id: String): Boolean {
        return getFavoriteIds().contains(id)
    }

    private fun getFavoriteIds(): Set<String> {
        return prefs?.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun saveUserNote(id: String, note: String) {
        prefs?.edit()?.putString(KEY_NOTE_PREFIX + id, note)?.apply()
    }

    fun getUserNote(id: String): String {
        return prefs?.getString(KEY_NOTE_PREFIX + id, "") ?: ""
    }
}
