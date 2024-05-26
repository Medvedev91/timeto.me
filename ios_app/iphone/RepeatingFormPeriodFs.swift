import SwiftUI
import shared

struct RepeatingFormPeriodFs: View {

    @State private var vm: RepeatingFormPeriodVm
    @Binding private var isPresented: Bool
    private let onPick: (RepeatingDbPeriod?) -> Void

    // Needed for SwiftUI picker, up via onChange(). Set in onAppear().
    @State private var pickerNDaysTemp = 2
    @State private var isAddDayOfYearSheetPresented = false

    init(
        isPresented: Binding<Bool>,
        defaultPeriod: RepeatingDbPeriod?,
        onPick: @escaping (RepeatingDbPeriod?) -> Void
    ) {
        _vm = State(initialValue: RepeatingFormPeriodVm(defaultPeriod: defaultPeriod))
        _isPresented = isPresented
        self.onPick = onPick
    }

    var body: some View {

        VMView(vm: vm, stack: .VStack()) { state in

            Fs__HeaderAction(
                title: state.title,
                actionText: state.doneText,
                scrollToHeader: 0,
                onCancel: {
                    isPresented = false
                },
                onDone: {
                    vm.buildSelectedPeriod { period in
                        onPick(period)
                        isPresented = false
                    }
                }
            )

            MyListView__PaddingFirst()

            ForEach(0..<state.periods.count, id: \.self) { periodIndex in

                let isFirst = periodIndex == 0

                MyListView__ItemView(
                    isFirst: isFirst,
                    isLast: state.periods.count == periodIndex + 1,
                    bgColor: c.fg,
                    withTopDivider: !isFirst
                ) {

                    VStack {

                        let isActive = state.activePeriodIndex?.toInt() == periodIndex

                        MyListView__ItemView__RadioView(
                            text: state.periods[periodIndex],
                            isActive: isActive
                        ) {
                            withAnimation {
                                vm.setActivePeriodIndex(index: isActive ? nil : periodIndex.toKotlinInt())
                            }
                        }

                        if isActive {
                            // todo use switch
                            if periodIndex == nil {
                            } else if periodIndex == 0 {
                            } else if periodIndex == 1 {
                                Picker(
                                    "",
                                    selection: $pickerNDaysTemp
                                ) {
                                    // Start from 1 to see WheelPicker
                                    ForEach(1..<667, id: \.self) {
                                        Text("\($0)").tag($0)
                                    }
                                }
                                .pickerStyle(WheelPickerStyle())
                                .onChange(of: pickerNDaysTemp) { newValue in
                                    vm.setSelectedNDays(nDays: newValue.toInt32())
                                }
                                .onAppear {
                                    pickerNDaysTemp = state.selectedNDays.toInt()
                                }
                                // The same problem with .DAY_OF_MONTH
                                // https://stackoverflow.com/a/62603630
                                // Can't decrease the height, the pressing area remains outside the limits
                                // "Add .compositingGroup() after .clipped()" "No longer works on iOS 15.1"
                            } else if periodIndex == 2 {
                                WeekDaysFormView(
                                    weekDays: state.selectedWeekDays,
                                    size: 32,
                                    onChange: { newWeekDays in
                                        vm.upWeekDays(newWeekDays: newWeekDays)
                                    }
                                )
                                .padding(.top, 4)
                                .padding(.bottom, 16)
                                .padding(.leading, MyListView.PADDING_INNER_HORIZONTAL - 1)
                            } else if periodIndex == 3 {
                                let dayNumbers: [Int] = Array(1..<(RepeatingDb.companion.MAX_DAY_OF_MONTH.toInt() + 1))
                                VStack(alignment: .leading, spacing: 8) {
                                    ForEach(dayNumbers.chunked(7), id: \.self) { chunk in
                                        HStack(spacing: 8) {
                                            ForEach(chunk, id: \.self) { day in
                                                let isDaySelected = state.selectedDaysOfMonth.contains(day.toKotlinInt())
                                                DayOfMonthItemView(
                                                    text: "\(day)",
                                                    isDaySelected: isDaySelected
                                                ) {
                                                    vm.toggleDayOfMonth(day: day.toInt32())
                                                }
                                            }
                                        }
                                    }
                                    let isDaySelected = state.selectedDaysOfMonth.contains(RepeatingDb.companion.LAST_DAY_OF_MONTH.toInt().toKotlinInt())
                                    DayOfMonthItemView(
                                        text: "Last Day of the Month",
                                        isDaySelected: isDaySelected,
                                        width: .infinity,
                                        hPaddings: 10
                                    ) {
                                        vm.toggleDayOfMonth(day: RepeatingDb.companion.LAST_DAY_OF_MONTH)
                                    }
                                }
                                .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                                .padding(.top, 6)
                                .padding(.bottom, 16)
                                .padding(.leading, MyListView.PADDING_INNER_HORIZONTAL - 1)
                            } else if periodIndex == 4 {

                                VStack(alignment: .leading, spacing: 0) {

                                    ForEach(state.selectedDaysOfYear, id: \.hash) { monthDayData in

                                        HStack {

                                            Button(
                                                action: {
                                                    vm.delDayOfTheYear(item: monthDayData)
                                                },
                                                label: {
                                                    Image(systemName: "minus.circle.fill")
                                                        .foregroundColor(.red)
                                                }
                                            )

                                            Text(monthDayData.getTitle(isShortOrLong: false))
                                        }
                                        .padding(.bottom, 10)
                                    }

                                    Button(
                                        action: {
                                            isAddDayOfYearSheetPresented = true
                                        },
                                        label: {
                                            Text("Add")
                                        }
                                    )
                                }
                                .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                                .padding(.leading, MyListView.PADDING_INNER_HORIZONTAL)
                                .padding(.bottom, 14)
                                .sheetEnv(isPresented: $isAddDayOfYearSheetPresented) {
                                    AddDayOfYearSheet(
                                        isPresented: $isAddDayOfYearSheetPresented
                                    ) { monthDayItem in
                                        vm.addDayOfTheYear(item: monthDayItem)
                                    }
                                }
                            } else {
                                fatalError()
                            }
                        }
                    }
                }
            }

            Spacer()
        }
        .background(c.bg)
    }
}

private struct DayOfMonthItemView: View {

    let text: String
    let isDaySelected: Bool
    var width: CGFloat = 28
    var height: CGFloat = 28
    var hPaddings: CGFloat = 0
    let onClick: () -> Void

    var body: some View {

        Button(
            action: {
                onClick()
            },
            label: {
                Text("\(text)")
                    .foregroundColor(isDaySelected ? .white : .primary)
                    .frame(width: width, height: height)
                    .font(.system(size: 14))
                    .padding(.horizontal, hPaddings)
            }
        )
        .background(
            ZStack {
                if (isDaySelected) {
                    roundedShape.fill(.blue)
                } else {
                    roundedShape.stroke(.primary, lineWidth: 1)
                }
            }
        )
    }
}

private struct AddDayOfYearSheet: View {

    @Binding var isPresented: Bool
    let onSelect: (RepeatingDbPeriodDaysOfYear.MonthDayItem) -> Void

    @State private var selectedMonthId = 1.toInt32()
    @State private var selectedDayId = 1.toInt32()

    @State private var pickerDayIds: [Int32] = Array(1...5)

    var body: some View {

        VStack {

            SheetHeaderView(
                onCancel: { isPresented.toggle() },
                title: "Day of the Year",
                doneText: "Add",
                isDoneEnabled: true,
                scrollToHeader: 0
            ) {
                isPresented = false
                onSelect(
                    RepeatingDbPeriodDaysOfYear.MonthDayItem(
                        monthId: selectedMonthId,
                        dayId: selectedDayId
                    )
                )
            }
            .padding(.bottom, 20)

            MyListView__Padding__SectionHeader()

            MyListView__ItemView(
                isFirst: true,
                isLast: true
            ) {

                Picker(
                    "",
                    selection: $selectedMonthId
                ) {
                    ForEach(RepeatingDbPeriodDaysOfYear.companion.months, id: \.id) { month in
                        Text(month.getName())
                            .tag(month.id)
                    }
                }
                .pickerStyle(WheelPickerStyle())
                .onChange(of: selectedMonthId) { _ in
                    withAnimation {
                        selectedDayId = 1
                    }
                    upPickerDays()
                }
            }

            MyListView__ItemView(
                isFirst: true,
                isLast: true
            ) {

                Picker(
                    "",
                    selection: $selectedDayId
                ) {
                    ForEach(pickerDayIds, id: \.self) {
                        Text("\($0)")
                            .tag($0)
                    }
                }
                .pickerStyle(WheelPickerStyle())
            }

            Spacer()
        }
        .background(c.sheetBg)
        .onAppear {
            upPickerDays()
        }
    }

    private func upPickerDays() {
        let month = RepeatingDbPeriodDaysOfYear.companion.months.first {
            $0.id == selectedMonthId
        }!
        pickerDayIds = Array(month.days.first...month.days.last)
    }
}
