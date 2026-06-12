Place the TensorFlow Lite model file here:
- spending_predictor.tflite

Expected input tensor:
- shape: [1, 8]
- float features in this order:
  1) salary
  2) fixedExpenses
  3) spentThisMonth
  4) savingsGoal
  5) remainingDays
  6) dailyBudget
  7) availableBudget
  8) dayOfMonth

Expected output tensor:
- shape: [1, 1]
- predicted month-end spending
