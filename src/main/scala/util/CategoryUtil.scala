package choosenearme

object CategoryUtil {
  def categoriesForCheckin(checkin: CheckinDetail): List[String] = {
    (for {
      venue <- checkin.venue
      val categories = venue.categories
      val categoryNames = categories.map(_.name)
      val parentNames = categories.flatMap(_.parents).flatten
    } yield categoryNames ++ parentNames).flatten.toList.distinct
  }

  val reverseMatchingMap: Map[String, List[String]] =
    Map(
      "Performing Arts" -> List("Performing Arts Venue", "Dance Studio", "Indie or Off Broadway Theater", "Strip Club"),
      "Visual Arts" -> List("Arcade", "Comedy Club", "Indie Movie Theater", "Art Museum", "Performing Arts Venue", "Dance Studio", "Indie or Off Broadway Theater"),
      "Music" -> List("Music Venue", "Music Store", "Jazz Club", "Piano Bar", "Rock Club", "Performing Arts Venue", "Concert Hall", "Opera House"),
      "Sports" -> List("Sports Bar", "Sporting Goods Shop", "Racetrack", "Stadium"),
      "Health & Wellness" -> List("Gym or Fitness Center", "Spa or Massage"),
      "Nutrition" -> List("Bakery", "Food and Drink Shop", "Cafeteria", "Gastropub"),
      "Gym & Fitness" -> List("Gym or Fitness Center"),
      "Literacy" -> List("College Library", "Library"),
      "Literature & Writing" -> List("College Library", "Library"),
      "Foreign Languages" -> List("Karaoke Bar", "Embassy or Consulate"),
      "ESL" -> List("Asian Restaurant"),
      "History & Geography" -> List("College History Building", "History Museum", "Historic Site", "Planetarium"),
      "Civics & Government" -> List("Law School", "Government Building"),
      "Economics" -> List("Bank", "Flea Market"),
      "Social Sciences" -> List(),
      "Health & Life Science" -> List("Hospital"),
      "Applied Science" -> List("Electronics Store", "Science Museum", "Tech Startup"),
      "Environmental Science" -> List("Planetarium", "Zoo or Aquarium", "Garden"),
      "Mathematics" -> List("College Math Building"),
      "Early Development" -> List("Hospital"),
      "Community Service" -> List("Hardware Store"),
      "Character Education" -> List(),
      "College & Career Prep" -> List("Library"),
      "Extra Curricular" -> List("University", "Sorority House", "Fraternity House"),
      "Parental Involvement" -> List("Baseball Stadium"),
      "Other" -> List())

  val matchingMap = reverseMatchingMap.invert
}
