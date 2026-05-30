package com.example.schreibenaufdeutsch.ui.common

data class Template(
    val icon: String,
    val topic: String,
    val subtitle: String,
    val category: String
)

val allTemplates = listOf(
    Template("📧", "E-Mail an den Chef", "DTB B2 • Formell", "Arbeit"),
    Template("📦", "Beschwerde: Paket", "Telc B2 • Formell", "Alltag"),
    Template("💬", "Forum: Umweltschutz", "Goethe B2 • Diskurs", "Prüfung"),
    Template("🏥", "Krankmeldung", "Alltag A2 • Formell", "Arbeit"),
    Template("🏠", "Anfrage: Wohnung", "Alltag B1 • Formell", "Alltag"),
    Template("🎉", "Einladung: Party", "Alltag A1 • Informell", "Alltag"),
    Template("📊", "Projekt-Update", "DTB C1 • Professionell", "Arbeit"),
    Template("📝", "Leserbrief: KI", "Goethe C1 • Argumentation", "Prüfung"),
    Template("👨‍💻", "Bewerbung: IT", "DTB B2 • Formell", "Arbeit"),
    Template("🍽️", "Reservierung: Restaurant", "Alltag A2 • Formell", "Alltag"),
    Template("🚌", "Beschwerde: Verspätung", "Telc B1 • Formell", "Alltag"),
    Template("💡", "Idee: Team-Event", "DTB B1 • Informell", "Arbeit")
)
