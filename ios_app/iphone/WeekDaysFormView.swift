import SwiftUI
import shared

struct WeekDaysFormView: View {

    let weekDays: [KotlinInt]
    let size: CGFloat
    let onChange: ([KotlinInt]) -> Void

    var body: some View {

        let formUI = WeekDaysFormUI(weekDays: weekDays)

        HStack(spacing: 10) {

            ForEach(formUI.weekDaysUI, id: \.idx) { weekDayUI in

                let isSelected = weekDayUI.isSelected

                Button(
                        action: {
                            withAnimation {
                                onChange(formUI.toggleWeekDay(idx: weekDayUI.idx))
                            }
                        },
                        label: {
                            Text(weekDayUI.title)
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
