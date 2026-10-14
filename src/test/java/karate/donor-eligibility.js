// Simple JavaScript program: can a person donate blood today?
// Rules: age 18-65, weight >= 50 kg, at least 90 days since last donation.
function(age, weight, daysSinceLastDonation) {
  if (age < 18 || age > 65) return 'Not eligible: age';
  if (weight < 50) return 'Not eligible: weight';
  if (daysSinceLastDonation < 90) return 'Not eligible: donated recently';
  return 'Eligible';
}
