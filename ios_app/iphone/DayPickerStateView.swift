import SwiftUI
import shared

struct DayPickerStateView: View {

    @State private var dateRange: ClosedRange<Date>
    private let minDayState: Int
    private let maxDayState: Int

    @State private var formDate: Date
    private let dayState: Int
    private let onDayChanged: (Int) -> Void

    init(
            day: Int,
            minDay: Int,
            maxDay: Int,
            onDayChanged: @escaping (Int) -> Void
    ) {
        _formDate = State(initialValue: unixDayToDate(day))
        dayState = day
        self.onDayChanged = onDayChanged

        _dateRange = State(initialValue: unixDayToDate(minDay)...unixDayToDate(maxDay))
        minDayState = minDay
        maxDayState = maxDay
    }

    var body: some View {
        DatePicker(
                "",
                selection: $formDate,
                in: dateRange,
                displayedComponents: [.date]
        )
                //////
                .onChange(of: formDate) { new in
                    let newDay = new.toUnixTime().localDay.toInt()
                    if (newDay != dayState) {
                        onDayChanged(newDay)
                    }
                }
                .onChange(of: dayState) { new in
                    formDate = unixDayToDate(new)
                }
                //////
                .onChange(of: minDayState) { new in
                    dateRange = unixDayToDate(new)...unixDayToDate(maxDayState)
                }
                .onChange(of: maxDayState) { new in
                    dateRange = unixDayToDate(minDayState)...unixDayToDate(new)
                }
    }
}

private func unixDayToDate(_ day: some FixedWidthInteger) -> Date {
    Date(timeIntervalSince1970: Double(UnixTime.companion.byLocalDay(localDay: Int32(day)).time))
}
