import SwiftUI
import shared

struct WeekDaysFormView: View {

    let weekDays: [KotlinInt]
    let size: CGFloat
    let onChange: ([KotlinInt]) -> Void

    var body: some View {

        let formUi = WeekDaysFormUi(weekDays: weekDays)

        HStack(spacing: 10) {

            ForEach(formUi.weekDaysUi, id: \.idx) { weekDayUi in

                let isSelected = weekDayUi.isSelected

                Button(
                        action: {
                            withAnimation {
                                onChange(formUi.toggleWeekDay(idx: weekDayUi.idx))
                            }
                        },
                        label: {
                            Text(weekDayUi.title)
                                    .font(.system(size: 16))
                        }
                )
                        .foregroundColor(isSelected ? .white : .primary)
                        .frame(width: size, height: size)
                        .background(
                                ZStack {
                                    if (isSelected) {
                                        roundedShape.fill(.blue)
                                    } else {
                                        roundedShape.stroke(.primary, lineWidth: 1)
                                    }
                                }
                        )
            }

            Spacer()
        }
    }
}
