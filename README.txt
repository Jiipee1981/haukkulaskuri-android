HAUKKULASKURI – Android testiprojekti v0.1

Toiminnot:
- mikrofonin kuuntelu
- 60 sekunnin mittaus
- kolme erillistä otosta
- haukkua/min tulos
- kolmen otoksen keskiarvo
- nollaus

TÄRKEÄÄ:
Tämä on ensimmäinen kenttätestiversio. Haukun tunnistus perustuu tässä vaiheessa
äänen RMS-kynnykseen ja 220 ms estoaikaan. Se ei vielä luotettavasti erottele puhetta,
tuulta, toista koiraa tai muita ääniä. Oikeilla hirvikoiraäänitteillä voidaan seuraavaksi
säätää kynnystä ja korvata tunnistus paremmalla ääni-/ML-luokittelulla.

APK:n tekeminen:
Avaa kansio Android Studiossa, anna Gradlen synkronoitua ja valitse Build > Build APK(s).
