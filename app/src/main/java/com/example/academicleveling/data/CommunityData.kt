package com.example.academicleveling.data

object CommunityData {

    data class OwnerInfo(
        val name: String, val title: String, val code: String,
        val subject: String, val grade: String, val diff: Difficulty,
        val timerMode: QuizTimerMode = QuizTimerMode.NONE, val timerSecs: Int = 0
    )

    fun getOwnerFor(id: Int): OwnerInfo = when (id) {

        // ── ROUND 1 (9001–9011) — Alphabetical by surname ────────────────
        9001 -> OwnerInfo("Kyla Carl Castillo",      "Philippine Revolution",   "KCC01", "History",        "Grade 10", Difficulty.MEDIUM)
        9002 -> OwnerInfo("Lalaine Kysha Cid",       "C++ Programming 101",     "LKC01", "Programming",    "Grade 11", Difficulty.MEDIUM)
        9003 -> OwnerInfo("Alyza Fuentes",           "The Solar System",        "AF001", "Science",        "Grade 7",  Difficulty.EASY)
        9004 -> OwnerInfo("Matt Daniel Guillen",     "World War II",            "MDG01", "History",        "Grade 10", Difficulty.MEDIUM)
        9005 -> OwnerInfo("Lawrence Angelo Laruya",  "Araling Panlipunan",      "LAL01", "AP",             "Grade 9",  Difficulty.MEDIUM)
        9006 -> OwnerInfo("Denmarc Maglipon",        "PC Hardware Basics",      "DM001", "I.T.",           "Grade 10", Difficulty.EASY)
        9007 -> OwnerInfo("John Aeron Monzon",       "Basketball & Sports",     "JAM01", "P.E.",           "Grade 9",  Difficulty.EASY)
        9008 -> OwnerInfo("Serge Edward Oliveros",   "Literature Classics",     "SEO01", "Literature",     "Grade 12", Difficulty.HARD)
        9009 -> OwnerInfo("Dave Sampaga",            "Jose Rizal's Life",       "DS001", "History",        "Grade 10", Difficulty.MEDIUM)
        9010 -> OwnerInfo("Heaven Gibson Tranilla",  "Human Rights",            "HGT01", "Social Studies", "Grade 11", Difficulty.MEDIUM)
        9011 -> OwnerInfo("Jerlaine Velasco",        "Arts & Design Basics",    "JV001", "Arts",           "Grade 8",  Difficulty.EASY)

        // ── ROUND 2 (9012–9022) — Same alphabetical order ────────────────
        9012 -> OwnerInfo("Kyla Carl Castillo",      "Cell Biology Basics",     "KCC02", "Science",        "Grade 9",  Difficulty.EASY)
        9013 -> OwnerInfo("Lalaine Kysha Cid",       "Basic Algebra",           "LKC02", "Math",           "Grade 8",  Difficulty.EASY)
        9014 -> OwnerInfo("Alyza Fuentes",           "English Vocabulary",      "AF002", "English",        "Grade 9",  Difficulty.MEDIUM)
        9015 -> OwnerInfo("Matt Daniel Guillen",     "Human Body Systems",      "MDG02", "Science",        "Grade 8",  Difficulty.EASY)
        9016 -> OwnerInfo("Lawrence Angelo Laruya",  "General Chemistry",       "LAL02", "Science",        "Grade 11", Difficulty.HARD)
        9017 -> OwnerInfo("Denmarc Maglipon",        "Networking Concepts",     "DM002", "I.T.",           "Grade 11", Difficulty.MEDIUM)
        9018 -> OwnerInfo("John Aeron Monzon",       "Music Theory 101",        "JAM02", "Music",          "Grade 8",  Difficulty.EASY)
        9019 -> OwnerInfo("Serge Edward Oliveros",   "Filipino Panitikan",      "SEO02", "Filipino",       "Grade 10", Difficulty.MEDIUM)
        9020 -> OwnerInfo("Dave Sampaga",            "HTML & CSS Basics",       "DS002", "I.T.",           "Grade 11", Difficulty.EASY)
        9021 -> OwnerInfo("Heaven Gibson Tranilla",  "Basic Economics",         "HGT02", "Economics",      "Grade 12", Difficulty.HARD)
        9022 -> OwnerInfo("Jerlaine Velasco",        "Filipino Grammar",        "JV002", "Filipino",       "Grade 9",  Difficulty.MEDIUM)

        // ── ROUND 3 (9023–9033) — Same alphabetical order ────────────────
        9023 -> OwnerInfo("Kyla Carl Castillo",      "English Grammar Review",  "KCC03", "English",        "Grade 8",  Difficulty.EASY)
        9024 -> OwnerInfo("Lalaine Kysha Cid",       "Physics Fundamentals",    "LKC03", "Science",        "Grade 10", Difficulty.MEDIUM)
        9025 -> OwnerInfo("Alyza Fuentes",           "Trigonometry Basics",     "AF003", "Math",           "Grade 11", Difficulty.HARD)
        9026 -> OwnerInfo("Matt Daniel Guillen",     "Python Basics",           "MDG03", "Programming",    "Grade 11", Difficulty.EASY)
        9027 -> OwnerInfo("Lawrence Angelo Laruya",  "Statistics Intro",        "LAL03", "Math",           "Grade 12", Difficulty.HARD)
        9028 -> OwnerInfo("Denmarc Maglipon",        "Philippine Geography",    "DM003", "AP",             "Grade 7",  Difficulty.EASY)
        9029 -> OwnerInfo("John Aeron Monzon",       "World Geography",         "JAM03", "Social Studies", "Grade 10", Difficulty.MEDIUM)
        9030 -> OwnerInfo("Serge Edward Oliveros",   "Environmental Science",   "SEO03", "Science",        "Grade 9",  Difficulty.MEDIUM)
        9031 -> OwnerInfo("Dave Sampaga",            "Pinoy Culture & Trivia",  "DS003", "Culture",        "Grade 8",  Difficulty.EASY)
        9032 -> OwnerInfo("Heaven Gibson Tranilla",  "Health & Nutrition",      "HGT03", "Health",         "Grade 9",  Difficulty.EASY)
        9033 -> OwnerInfo("Jerlaine Velasco",        "Earth Science",           "JV003", "Science",        "Grade 10", Difficulty.MEDIUM)

        else  -> OwnerInfo("Unknown", "General Quiz", "GEN00", "General", "All", Difficulty.EASY)
    }

    // ══════════════════════════════════════════════════════════════════════
    //  QUESTIONS — 5 per quiz, IDs follow round order above
    // ══════════════════════════════════════════════════════════════════════

    fun getQuestionsFor(id: Int): List<QuizQuestion> = when (id) {

        // ── KYLA CARL CASTILLO ─────────────────────────────────────────

        9001 -> listOf( // Philippine Revolution
            QuizQuestion("Who is called the 'Brains of the Revolution'?", listOf("Apolinario Mabini", "Jose Rizal", "Antonio Luna", "Marcelo del Pilar"), 0, "Apolinario Mabini was the chief adviser of Aguinaldo.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("The Philippine Revolution against Spain began in 1896.", emptyList(), 0, "The Cry of Pugad Lawin was in August 1896.", QuizType.TRUE_FALSE),
            QuizQuestion("Who was the first President of the Philippines?", emptyList(), 0, "Emilio Aguinaldo", QuizType.IDENTIFICATION),
            QuizQuestion("GomBurZa refers to three martyred Filipino priests.", emptyList(), 0, "Gomez, Burgos, and Zamora were executed in 1872.", QuizType.TRUE_FALSE),
            QuizQuestion("Which was Rizal's first novel?", listOf("El Filibusterismo", "Noli Me Tangere", "Florante at Laura", "Ibong Adarna"), 1, "Noli Me Tangere was published in 1887.", QuizType.MULTIPLE_CHOICE)
        )

        9012 -> listOf( // Cell Biology Basics
            QuizQuestion("What is the fluid found inside the cell membrane called?", emptyList(), 0, "Cytoplasm", QuizType.IDENTIFICATION),
            QuizQuestion("All cells come from pre-existing cells.", emptyList(), 0, "This is part of the Cell Theory.", QuizType.TRUE_FALSE),
            QuizQuestion("Which organelle directs all cell activities?", listOf("Nucleus", "Cell Wall", "Vacuole", "Ribosome"), 0, "The nucleus contains DNA and controls the cell.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("Ribosomes are responsible for making proteins.", emptyList(), 0, "Ribosomes synthesize proteins.", QuizType.TRUE_FALSE),
            QuizQuestion("Which is an example of a prokaryote?", listOf("Human cell", "Fungi", "Bacteria", "Plant cell"), 2, "Bacteria are prokaryotes — no membrane-bound nucleus.", QuizType.MULTIPLE_CHOICE)
        )

        9023 -> listOf( // English Grammar Review
            QuizQuestion("What is the past tense of 'Speak'?", emptyList(), 0, "Spoke", QuizType.IDENTIFICATION),
            QuizQuestion("The word 'quickly' is an adverb.", emptyList(), 0, "Adverbs modify verbs, adjectives, or other adverbs.", QuizType.TRUE_FALSE),
            QuizQuestion("Which part of speech names a person, place, or thing?", listOf("Verb", "Noun", "Adjective", "Pronoun"), 1, "Nouns name people, places, things, or ideas.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("What is the plural of 'Mouse'?", emptyList(), 0, "Mice", QuizType.IDENTIFICATION),
            QuizQuestion("'Apple' is a proper noun.", emptyList(), 1, "Apple is a common noun; 'Apple Inc.' would be proper.", QuizType.TRUE_FALSE)
        )

        // ── LALAINE KYSHA CID ─────────────────────────────────────────

        9002 -> listOf( // C++ Programming 101
            QuizQuestion("Who created the C++ programming language?", listOf("Bjarne Stroustrup", "James Gosling", "Dennis Ritchie", "Guido van Rossum"), 0, "Bjarne Stroustrup created C++ in 1985.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("C++ supports object-oriented programming.", emptyList(), 0, "C++ supports OOP, procedural, and generic programming.", QuizType.TRUE_FALSE),
            QuizQuestion("What operator is used for input in C++?", emptyList(), 0, ">>", QuizType.IDENTIFICATION),
            QuizQuestion("What symbol ends a statement in C++?", emptyList(), 0, ";", QuizType.IDENTIFICATION),
            QuizQuestion("The 'float' data type is used for whole numbers only.", emptyList(), 1, "Float is for decimal numbers; 'int' is for whole numbers.", QuizType.TRUE_FALSE)
        )

        9013 -> listOf( // Basic Algebra
            QuizQuestion("What is x if x + 5 = 12?", emptyList(), 0, "7", QuizType.IDENTIFICATION),
            QuizQuestion("In the expression 4y, what is the coefficient?", listOf("4", "y", "4y", "None"), 0, "The coefficient is the number multiplied by the variable.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("A straight angle measures 180 degrees.", emptyList(), 0, "A straight line forms a 180-degree angle.", QuizType.TRUE_FALSE),
            QuizQuestion("What is the result of a negative number times a negative number?", emptyList(), 0, "Positive", QuizType.IDENTIFICATION),
            QuizQuestion("The variable 'x' is commonly used in algebraic equations.", emptyList(), 0, "X is the most commonly used variable in algebra.", QuizType.TRUE_FALSE)
        )

        9024 -> listOf( // Physics Fundamentals
            QuizQuestion("Energy can be created from nothing.", emptyList(), 1, "Energy cannot be created or destroyed — Law of Conservation of Energy.", QuizType.TRUE_FALSE),
            QuizQuestion("What is the unit of electrical resistance?", emptyList(), 0, "Ohm", QuizType.IDENTIFICATION),
            QuizQuestion("Speed equals Distance divided by what?", emptyList(), 0, "Time", QuizType.IDENTIFICATION),
            QuizQuestion("Newton's Law of Inertia is his ___ Law of Motion.", listOf("1st", "2nd", "3rd", "4th"), 0, "The 1st Law: an object at rest stays at rest.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("Sound can travel through a vacuum.", emptyList(), 1, "Sound needs a medium to travel; it cannot travel in a vacuum.", QuizType.TRUE_FALSE)
        )

        // ── ALYZA FUENTES ─────────────────────────────────────────────

        9003 -> listOf( // The Solar System
            QuizQuestion("What is Earth's only natural satellite?", emptyList(), 0, "Moon", QuizType.IDENTIFICATION),
            QuizQuestion("Mars is considered a cold planet.", emptyList(), 0, "Mars has an average temperature of about -60 degrees Celsius.", QuizType.TRUE_FALSE),
            QuizQuestion("Pluto is classified as a dwarf planet.", emptyList(), 0, "Pluto was reclassified by the IAU in 2006.", QuizType.TRUE_FALSE),
            QuizQuestion("Which planet is closest to the Sun?", listOf("Earth", "Mars", "Mercury", "Venus"), 2, "Mercury is the innermost planet in our Solar System.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("How many stars are in our Solar System?", emptyList(), 0, "1", QuizType.IDENTIFICATION)
        )

        9014 -> listOf( // English Vocabulary
            QuizQuestion("What does 'Benevolent' mean?", listOf("Evil", "Kind and generous", "Fast-moving", "Very strong"), 1, "Benevolent means well-meaning and generous.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("What is the opposite of 'Arrival'?", emptyList(), 0, "Departure", QuizType.IDENTIFICATION),
            QuizQuestion("What do you call a shortened form of two words joined together?", emptyList(), 0, "Contraction", QuizType.IDENTIFICATION),
            QuizQuestion("'The' is classified as an article.", emptyList(), 0, "The, a, and an are the three articles in English.", QuizType.TRUE_FALSE),
            QuizQuestion("A synonym is a word that has the opposite meaning.", emptyList(), 1, "A synonym has a similar meaning; an antonym has the opposite.", QuizType.TRUE_FALSE)
        )

        9025 -> listOf( // Trigonometry Basics
            QuizQuestion("The hypotenuse is the shortest side of a right triangle.", emptyList(), 1, "The hypotenuse is the longest side, opposite the right angle.", QuizType.TRUE_FALSE),
            QuizQuestion("Tangent equals Opposite divided by what?", emptyList(), 0, "Adjacent", QuizType.IDENTIFICATION),
            QuizQuestion("What is the sum of interior angles of a square?", listOf("90 degrees", "180 degrees", "360 degrees", "270 degrees"), 2, "A square has four 90-degree angles totaling 360 degrees.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("Sin 90 degrees equals 1.", emptyList(), 0, "Sin 90 = 1 is a fundamental trigonometric value.", QuizType.TRUE_FALSE),
            QuizQuestion("Who is known as the Father of Geometry?", emptyList(), 0, "Euclid", QuizType.IDENTIFICATION)
        )

        // ── MATT DANIEL GUILLEN ───────────────────────────────────────

        9004 -> listOf( // World War II
            QuizQuestion("In what year did World War II end?", listOf("1943", "1944", "1945", "1946"), 2, "WWII ended in 1945 with Germany and Japan's surrender.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("The atomic bomb was dropped on Hiroshima and Nagasaki.", emptyList(), 0, "The US dropped bombs in August 1945.", QuizType.TRUE_FALSE),
            QuizQuestion("Who led Nazi Germany during World War II?", emptyList(), 0, "Adolf Hitler", QuizType.IDENTIFICATION),
            QuizQuestion("The D-Day invasion took place in Normandy, France.", emptyList(), 0, "Operation Overlord launched on June 6, 1944.", QuizType.TRUE_FALSE),
            QuizQuestion("Which country was NOT part of the Allied Powers?", listOf("USA", "United Kingdom", "Germany", "USSR"), 2, "Germany was part of the Axis Powers.", QuizType.MULTIPLE_CHOICE)
        )

        9015 -> listOf( // Human Body Systems
            QuizQuestion("Which organ pumps blood throughout the body?", listOf("Lung", "Kidney", "Heart", "Liver"), 2, "The heart is the central organ of the circulatory system.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("The skeletal system provides structure and support for the body.", emptyList(), 0, "Bones give the body its shape and protect internal organs.", QuizType.TRUE_FALSE),
            QuizQuestion("What is the largest organ of the human body?", emptyList(), 0, "Skin", QuizType.IDENTIFICATION),
            QuizQuestion("The brain is part of the nervous system.", emptyList(), 0, "The brain and spinal cord form the central nervous system.", QuizType.TRUE_FALSE),
            QuizQuestion("Where does digestion begin?", listOf("Stomach", "Small Intestine", "Mouth", "Esophagus"), 2, "Digestion starts in the mouth with chewing and saliva.", QuizType.MULTIPLE_CHOICE)
        )

        9026 -> listOf( // Python Basics
            QuizQuestion("Which function displays output in Python?", listOf("echo()", "print()", "display()", "show()"), 1, "print() outputs text to the console in Python.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("Python uses indentation to define code blocks.", emptyList(), 0, "Unlike other languages, Python relies on indentation, not braces.", QuizType.TRUE_FALSE),
            QuizQuestion("What symbol starts a comment in Python?", emptyList(), 0, "#", QuizType.IDENTIFICATION),
            QuizQuestion("Python is a case-sensitive language.", emptyList(), 0, "'Name' and 'name' are different variables in Python.", QuizType.TRUE_FALSE),
            QuizQuestion("Which keyword is used to define a function in Python?", listOf("function", "def", "fun", "define"), 1, "The 'def' keyword defines a function in Python.", QuizType.MULTIPLE_CHOICE)
        )

        // ── LAWRENCE ANGELO LARUYA ────────────────────────────────────

        9005 -> listOf( // Araling Panlipunan
            QuizQuestion("Ano ang pangunahing ilog ng Pilipinas?", listOf("Cagayan River", "Pasig River", "Agusan River", "Pampanga River"), 0, "Ang Cagayan River ang pinakamahaba sa Pilipinas.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("Si Manuel L. Quezon ang unang Pangulo ng Komonwelt.", emptyList(), 0, "Siya ay nanungkulan mula 1935 hanggang 1944.", QuizType.TRUE_FALSE),
            QuizQuestion("Ano ang kabisera ng Pilipinas?", emptyList(), 0, "Maynila", QuizType.IDENTIFICATION),
            QuizQuestion("Ang ASEAN ay binubuo ng sampung miyembrong bansa.", emptyList(), 0, "Ang ASEAN ay itinatag noong 1967.", QuizType.TRUE_FALSE),
            QuizQuestion("Saang rehiyon matatagpuan ang Chocolate Hills?", listOf("Palawan", "Bohol", "Cebu", "Davao"), 1, "Ang Chocolate Hills ay nasa Bohol.", QuizType.MULTIPLE_CHOICE)
        )

        9016 -> listOf( // General Chemistry
            QuizQuestion("What is the chemical symbol for Gold?", emptyList(), 0, "Au", QuizType.IDENTIFICATION),
            QuizQuestion("An atom is the smallest unit of an element.", emptyList(), 0, "Atoms are the basic building blocks of matter.", QuizType.TRUE_FALSE),
            QuizQuestion("Which gas makes up most of Earth's atmosphere?", listOf("Oxygen", "Carbon Dioxide", "Nitrogen", "Hydrogen"), 2, "Nitrogen makes up about 78% of Earth's atmosphere.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("What is the pH of a neutral solution?", emptyList(), 0, "7", QuizType.IDENTIFICATION),
            QuizQuestion("Ionic bonds form between a metal and a nonmetal.", emptyList(), 0, "Ionic bonds result from the transfer of electrons.", QuizType.TRUE_FALSE)
        )

        9027 -> listOf( // Statistics Intro
            QuizQuestion("What is the middle value in a sorted data set called?", emptyList(), 0, "Median", QuizType.IDENTIFICATION),
            QuizQuestion("The mean is the most frequently occurring value in a data set.", emptyList(), 1, "The mode is the most frequent; the mean is the average.", QuizType.TRUE_FALSE),
            QuizQuestion("Which measure shows how spread out data is?", listOf("Mean", "Mode", "Median", "Standard Deviation"), 3, "Standard deviation measures the spread of data.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("Probability values range from 0 to 1.", emptyList(), 0, "0 means impossible; 1 means certain.", QuizType.TRUE_FALSE),
            QuizQuestion("What is the average of 5, 10, and 15?", emptyList(), 0, "10", QuizType.IDENTIFICATION)
        )

        // ── DENMARC MAGLIPON ──────────────────────────────────────────

        9006 -> listOf( // PC Hardware Basics
            QuizQuestion("Which component is known as the brain of the computer?", listOf("RAM", "GPU", "CPU", "HDD"), 2, "The CPU handles all instructions.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("RAM stands for Random Access Memory.", emptyList(), 0, "RAM is temporary, fast-access memory used while the PC runs.", QuizType.TRUE_FALSE),
            QuizQuestion("What does GPU stand for?", emptyList(), 0, "Graphics Processing Unit", QuizType.IDENTIFICATION),
            QuizQuestion("An SSD is faster than a traditional HDD.", emptyList(), 0, "SSDs use flash memory and have no moving parts.", QuizType.TRUE_FALSE),
            QuizQuestion("Which port is commonly used to connect a keyboard?", listOf("HDMI", "USB", "VGA", "Ethernet"), 1, "USB is the standard connection for keyboards and mice.", QuizType.MULTIPLE_CHOICE)
        )

        9017 -> listOf( // Networking Concepts
            QuizQuestion("What does IP stand for in networking?", emptyList(), 0, "Internet Protocol", QuizType.IDENTIFICATION),
            QuizQuestion("A router connects different networks together.", emptyList(), 0, "Routers direct data packets between networks.", QuizType.TRUE_FALSE),
            QuizQuestion("Which device connects multiple devices in a local network?", listOf("Modem", "Switch", "Router", "Hub"), 1, "A switch connects devices within the same LAN.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("HTTP stands for HyperText Transfer Protocol.", emptyList(), 0, "HTTP is the foundation of data communication on the Web.", QuizType.TRUE_FALSE),
            QuizQuestion("What is the standard port number for HTTPS?", emptyList(), 0, "443", QuizType.IDENTIFICATION)
        )

        9028 -> listOf( // Philippine Geography
            QuizQuestion("How many islands make up the Philippine archipelago?", listOf("5,000", "7,641", "10,000", "3,200"), 1, "The Philippines has 7,641 islands.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("Mount Apo is the highest mountain in the Philippines.", emptyList(), 0, "Mount Apo stands at 2,954 meters above sea level.", QuizType.TRUE_FALSE),
            QuizQuestion("What is the largest island in the Philippines?", emptyList(), 0, "Luzon", QuizType.IDENTIFICATION),
            QuizQuestion("Palawan is known as the Last Frontier of the Philippines.", emptyList(), 0, "Palawan is famous for its biodiversity and natural beauty.", QuizType.TRUE_FALSE),
            QuizQuestion("Which body of water is to the west of the Philippines?", listOf("Pacific Ocean", "Celebes Sea", "South China Sea", "Sulu Sea"), 2, "The South China Sea (West Philippine Sea) is to the west.", QuizType.MULTIPLE_CHOICE)
        )

        // ── JOHN AERON MONZON ─────────────────────────────────────────

        9007 -> listOf( // Basketball & Sports
            QuizQuestion("How many players are on a basketball team on the court?", emptyList(), 0, "5", QuizType.IDENTIFICATION),
            QuizQuestion("A free throw is worth 1 point in basketball.", emptyList(), 0, "Each successful free throw scores 1 point.", QuizType.TRUE_FALSE),
            QuizQuestion("Which country invented basketball?", listOf("USA", "Canada", "UK", "Spain"), 0, "Basketball was invented by Dr. James Naismith in the USA.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("Dribbling with both hands simultaneously is a violation.", emptyList(), 0, "This is called a double dribble.", QuizType.TRUE_FALSE),
            QuizQuestion("How many points is a shot beyond the three-point line worth?", emptyList(), 0, "3", QuizType.IDENTIFICATION)
        )

        9018 -> listOf( // Music Theory 101
            QuizQuestion("How many beats does a whole note receive in 4/4 time?", emptyList(), 0, "4", QuizType.IDENTIFICATION),
            QuizQuestion("The treble clef is also known as the G clef.", emptyList(), 0, "The treble clef circles the G line on the staff.", QuizType.TRUE_FALSE),
            QuizQuestion("How many lines does a musical staff have?", listOf("3", "4", "5", "6"), 2, "A standard musical staff has 5 lines.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("A sharp symbol raises a note by one half step.", emptyList(), 0, "The sharp raises the pitch by a semitone.", QuizType.TRUE_FALSE),
            QuizQuestion("What is the term for the speed of music?", emptyList(), 0, "Tempo", QuizType.IDENTIFICATION)
        )

        9029 -> listOf( // World Geography
            QuizQuestion("What is the largest continent by area?", emptyList(), 0, "Asia", QuizType.IDENTIFICATION),
            QuizQuestion("The Amazon River is the longest river in the world.", emptyList(), 1, "The Nile River is generally considered the longest.", QuizType.TRUE_FALSE),
            QuizQuestion("Which country has the most natural lakes?", listOf("USA", "Russia", "Canada", "Brazil"), 2, "Canada has over 60% of the world's fresh water lakes.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("Mount Everest is located in the Himalayas.", emptyList(), 0, "Everest sits on the Nepal-Tibet border.", QuizType.TRUE_FALSE),
            QuizQuestion("What is the capital city of Australia?", emptyList(), 0, "Canberra", QuizType.IDENTIFICATION)
        )

        // ── SERGE EDWARD OLIVEROS ─────────────────────────────────────

        9008 -> listOf( // Literature Classics
            QuizQuestion("Who wrote Romeo and Juliet?", emptyList(), 0, "William Shakespeare", QuizType.IDENTIFICATION),
            QuizQuestion("'To Kill a Mockingbird' was written by Harper Lee.", emptyList(), 0, "Published in 1960, it won the Pulitzer Prize.", QuizType.TRUE_FALSE),
            QuizQuestion("Which Shakespeare play features the character Hamlet?", listOf("Macbeth", "Othello", "Hamlet", "King Lear"), 2, "Hamlet is the Prince of Denmark in the play.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("Romeo and Juliet is set in the city of Verona.", emptyList(), 0, "The play is set in Verona, Italy.", QuizType.TRUE_FALSE),
            QuizQuestion("What literary device uses 'the wind whispered'?", emptyList(), 0, "Personification", QuizType.IDENTIFICATION)
        )

        9019 -> listOf( // Filipino Panitikan
            QuizQuestion("Sino ang sumulat ng 'Florante at Laura'?", emptyList(), 0, "Francisco Balagtas", QuizType.IDENTIFICATION),
            QuizQuestion("Ang 'Ibong Adarna' ay isang epikong tula.", emptyList(), 0, "Ito ay isang korido na nagtatampok ng mahiwagang ibon.", QuizType.TRUE_FALSE),
            QuizQuestion("Anong uri ng akda ang 'Noli Me Tangere'?", listOf("Tula", "Nobela", "Dula", "Sanaysay"), 1, "Ang Noli Me Tangere ay isang nobela ni Jose Rizal.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("Ang haiku ay binubuo ng tatlong linya.", emptyList(), 0, "Ang haiku ay may 5-7-5 na pantig na estruktura.", QuizType.TRUE_FALSE),
            QuizQuestion("Anong tawag sa taludtod na may sukat at tugma?", emptyList(), 0, "Patula", QuizType.IDENTIFICATION)
        )

        9030 -> listOf( // Environmental Science
            QuizQuestion("What gas do plants absorb during photosynthesis?", emptyList(), 0, "Carbon Dioxide", QuizType.IDENTIFICATION),
            QuizQuestion("Deforestation contributes to climate change.", emptyList(), 0, "Removing trees reduces CO2 absorption.", QuizType.TRUE_FALSE),
            QuizQuestion("Which layer of the atmosphere protects us from UV rays?", listOf("Troposphere", "Stratosphere", "Mesosphere", "Thermosphere"), 1, "The ozone layer is in the stratosphere.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("Renewable energy sources include solar and wind power.", emptyList(), 0, "These are replenished naturally and are sustainable.", QuizType.TRUE_FALSE),
            QuizQuestion("What is the term for the variety of life on Earth?", emptyList(), 0, "Biodiversity", QuizType.IDENTIFICATION)
        )

        // ── DAVE SAMPAGA ──────────────────────────────────────────────

        9009 -> listOf( // Jose Rizal's Life
            QuizQuestion("In what year was Jose Rizal born?", emptyList(), 0, "1861", QuizType.IDENTIFICATION),
            QuizQuestion("Rizal was executed at Bagumbayan, now known as Luneta Park.", emptyList(), 0, "He was executed by firing squad on December 30, 1896.", QuizType.TRUE_FALSE),
            QuizQuestion("Which country did Rizal study medicine in?", listOf("France", "Germany", "Spain", "UK"), 2, "Rizal studied medicine at the Universidad Central de Madrid.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("Rizal's second novel is El Filibusterismo.", emptyList(), 0, "El Fili was published in 1891 as a sequel to Noli.", QuizType.TRUE_FALSE),
            QuizQuestion("What organization did Rizal found in 1892?", emptyList(), 0, "La Liga Filipina", QuizType.IDENTIFICATION)
        )

        9020 -> listOf( // HTML & CSS Basics
            QuizQuestion("What does HTML stand for?", emptyList(), 0, "HyperText Markup Language", QuizType.IDENTIFICATION),
            QuizQuestion("CSS is used to style HTML elements.", emptyList(), 0, "CSS controls layout, colors, fonts, and more.", QuizType.TRUE_FALSE),
            QuizQuestion("Which HTML tag is used for the largest heading?", listOf("<h6>", "<h3>", "<h1>", "<head>"), 2, "<h1> is the largest and most important heading.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("The <a> tag is used to create hyperlinks.", emptyList(), 0, "The anchor tag with href creates a clickable link.", QuizType.TRUE_FALSE),
            QuizQuestion("Which CSS property changes text color?", emptyList(), 0, "color", QuizType.IDENTIFICATION)
        )

        9031 -> listOf( // Pinoy Culture & Trivia
            QuizQuestion("What is the national flower of the Philippines?", emptyList(), 0, "Sampaguita", QuizType.IDENTIFICATION),
            QuizQuestion("Lechon is considered one of the most famous Filipino dishes.", emptyList(), 0, "Lechon is a whole roasted pig, popular at celebrations.", QuizType.TRUE_FALSE),
            QuizQuestion("Which Filipino value emphasizes community and togetherness?", listOf("Utang na loob", "Bayanihan", "Hiya", "Pakikisama"), 1, "Bayanihan is the spirit of communal unity and cooperation.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("The Philippine flag has three stars representing Luzon, Visayas, and Mindanao.", emptyList(), 0, "The three stars symbolize the three major island groups.", QuizType.TRUE_FALSE),
            QuizQuestion("What is the traditional Filipino dance where you jump over bamboo poles?", emptyList(), 0, "Tinikling", QuizType.IDENTIFICATION)
        )

        // ── HEAVEN GIBSON TRANILLA ────────────────────────────────────

        9010 -> listOf( // Human Rights
            QuizQuestion("The Universal Declaration of Human Rights was adopted in what year?", emptyList(), 0, "1948", QuizType.IDENTIFICATION),
            QuizQuestion("Human rights apply to all people regardless of nationality.", emptyList(), 0, "Human rights are universal and inalienable.", QuizType.TRUE_FALSE),
            QuizQuestion("Which organization is primarily responsible for protecting human rights globally?", listOf("NATO", "WHO", "United Nations", "World Bank"), 2, "The UN was established to promote peace and human rights.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("Freedom of speech is a recognized human right.", emptyList(), 0, "It is protected under the UDHR Article 19.", QuizType.TRUE_FALSE),
            QuizQuestion("What is the term for unfair treatment based on race or gender?", emptyList(), 0, "Discrimination", QuizType.IDENTIFICATION)
        )

        9021 -> listOf( // Basic Economics
            QuizQuestion("What is the term for the total value of goods and services produced by a country?", emptyList(), 0, "GDP", QuizType.IDENTIFICATION),
            QuizQuestion("Inflation refers to a general increase in prices over time.", emptyList(), 0, "Inflation reduces the purchasing power of money.", QuizType.TRUE_FALSE),
            QuizQuestion("Which type of economy is controlled by the government?", listOf("Market Economy", "Mixed Economy", "Command Economy", "Traditional Economy"), 2, "In a command economy, the government makes all economic decisions.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("Supply and demand are key concepts in economics.", emptyList(), 0, "They determine the price and quantity of goods in a market.", QuizType.TRUE_FALSE),
            QuizQuestion("What is it called when a government spends more than it earns?", emptyList(), 0, "Budget Deficit", QuizType.IDENTIFICATION)
        )

        9032 -> listOf( // Health & Nutrition
            QuizQuestion("Which vitamin is produced when skin is exposed to sunlight?", emptyList(), 0, "Vitamin D", QuizType.IDENTIFICATION),
            QuizQuestion("Drinking 8 glasses of water daily is recommended for good health.", emptyList(), 0, "Staying hydrated supports all body functions.", QuizType.TRUE_FALSE),
            QuizQuestion("Which nutrient provides the most energy per gram?", listOf("Protein", "Carbohydrates", "Fat", "Vitamins"), 2, "Fat provides 9 calories per gram, the most of any nutrient.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("Vegetables are an important source of dietary fiber.", emptyList(), 0, "Fiber aids digestion and helps maintain a healthy weight.", QuizType.TRUE_FALSE),
            QuizQuestion("What is the main function of red blood cells?", emptyList(), 0, "Carry Oxygen", QuizType.IDENTIFICATION)
        )

        // ── JERLAINE VELASCO ──────────────────────────────────────────

        9011 -> listOf( // Arts & Design Basics
            QuizQuestion("What are the three primary colors?", emptyList(), 0, "Red, Blue, Yellow", QuizType.IDENTIFICATION),
            QuizQuestion("Watercolor and oil paint are both types of painting media.", emptyList(), 0, "Both are widely used in visual arts.", QuizType.TRUE_FALSE),
            QuizQuestion("Which element of art refers to the lightness or darkness of a color?", listOf("Hue", "Value", "Saturation", "Texture"), 1, "Value describes how light or dark a color appears.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("The rule of thirds is a composition guideline in art and photography.", emptyList(), 0, "It helps create balanced and visually interesting compositions.", QuizType.TRUE_FALSE),
            QuizQuestion("What is the term for a sculpture made by carving away material?", emptyList(), 0, "Subtractive Sculpture", QuizType.IDENTIFICATION)
        )

        9022 -> listOf( // Filipino Grammar
            QuizQuestion("Anong uri ng pangngalan ang 'Guro'?", listOf("Pantangi", "Pambalana", "Kolektibo", "Konkreto"), 1, "Ang 'guro' ay pambalanang pangngalan.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("Ang pandiwa ay nagpapahayag ng kilos o galaw.", emptyList(), 0, "Halimbawa: tumakbo, kumain, maglaro.", QuizType.TRUE_FALSE),
            QuizQuestion("Ano ang salitang-ugat ng 'pagkain'?", emptyList(), 0, "kain", QuizType.IDENTIFICATION),
            QuizQuestion("Ang pang-uri ay naglalarawan sa pangngalan o panghalip.", emptyList(), 0, "Halimbawa: maganda, matangkad, masipag.", QuizType.TRUE_FALSE),
            QuizQuestion("Alin ang wastong baybay?", listOf("Araw-araw", "Araw araw", "Arawaraw", "Aaraw"), 0, "Ang tamang baybay ay 'araw-araw' na may gitling.", QuizType.MULTIPLE_CHOICE)
        )

        9033 -> listOf( // Earth Science
            QuizQuestion("What are the three types of rocks?", emptyList(), 0, "Igneous, Sedimentary, Metamorphic", QuizType.IDENTIFICATION),
            QuizQuestion("The Earth's core is made primarily of iron and nickel.", emptyList(), 0, "The inner core is a solid iron-nickel alloy.", QuizType.TRUE_FALSE),
            QuizQuestion("Which layer of the Earth do we live on?", listOf("Mantle", "Core", "Crust", "Asthenosphere"), 2, "We live on the Earth's crust, the outermost layer.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("Earthquakes are measured using the Richter scale.", emptyList(), 0, "The Richter scale measures the magnitude of earthquakes.", QuizType.TRUE_FALSE),
            QuizQuestion("What is the process where water vapor turns into liquid water?", emptyList(), 0, "Condensation", QuizType.IDENTIFICATION)
        )

        else -> listOf(
            QuizQuestion("Is studying important for success?", emptyList(), 0, "True", QuizType.TRUE_FALSE),
            QuizQuestion("What is the goal of this quiz?", listOf("Fail", "Learn", "Sleep", "Eat"), 1, "Learning is the goal.", QuizType.MULTIPLE_CHOICE),
            QuizQuestion("Type 'READY' to continue.", emptyList(), 0, "READY", QuizType.IDENTIFICATION),
            QuizQuestion("What is 10 multiplied by 10?", emptyList(), 0, "100", QuizType.IDENTIFICATION),
            QuizQuestion("Keep going — you're doing great!", listOf("Yes", "No", "Maybe", "Later"), 0, "Yes!", QuizType.MULTIPLE_CHOICE)
        )
    }

    fun getDateFor(id: Int): String {
        val days   = (id % 28) + 1
        val months = (id % 12) + 1
        return "2024-${months.toString().padStart(2, '0')}-${days.toString().padStart(2, '0')}"
    }
}