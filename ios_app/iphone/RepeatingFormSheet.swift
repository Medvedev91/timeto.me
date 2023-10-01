import SwiftUI
import shared

struct RepeatingsFormSheet: View {

    @State private var vm: RepeatingFormSheetVM
    @Binding private var isPresented: Bool
    private let onSave: () -> ()

    // Needed for SwiftUI picker, up via onChange(). Set in onAppear().
    @State private var pickerNDaysTemp = 2

    @State private var isAddDayOfYearSheetPresented = false
    @State private var isDaytimeSheetPresented = false

    @State private var sheetHeaderScroll = 0

    init(
            isPresented: Binding<Bool>,
            editedRepeating: RepeatingModel?,
            onSave: @escaping () -> ()
    ) {
        _isPresented = isPresented
        self.onSave = onSave
        vm = RepeatingFormSheetVM(repeating: editedRepeating)
    }

    var body: some View {

        VMView(vm: vm, stack: .VStack()) { state in

            SheetHeaderView(
                    onCancel: { isPresented.toggle() },
                    title: state.headerTitle,
                    doneText: state.headerDoneText,
                    isDoneEnabled: state.isHeaderDoneEnabled,
                    scrollToHeader: sheetHeaderScroll
            ) {
                vm.save {
                    onSave()
                    isPresented = false
                }
            }

            ScrollViewWithVListener(showsIndicators: false, vScroll: $sheetHeaderScroll) {

                VStack {

                    VStack {

                        MyListView__ItemView(
                                isFirst: true,
                                isLast: true
                        ) {

                            MyListView__ItemView__TextInputView(
                                    text: state.inputTextValue,
                                    placeholder: "Task",
                                    isAutofocus: false
                            ) { newValue in
                                vm.setTextValue(text: newValue)
                            }
                        }
                                .padding(.top, 10)

                        MyListView__Padding__SectionSection()

                        TextFeaturesTriggersFormView(
                                textFeatures: state.textFeatures
                        ) { textFeatures in
                            vm.upTextFeatures(textFeatures: textFeatures)
                        }

                        MyListView__Padding__SectionSection()

                        TextFeaturesTimerFormView(
                                textFeatures: state.textFeatures
                        ) { textFeatures in
                            vm.upTextFeatures(textFeatures: textFeatures)
                        }

                        MyListView__Padding__SectionSection()

                        MyListView__ItemView(
                                isFirst: true,
                                isLast: false
                        ) {

                            MyListView__ItemView__ButtonView(
                                    text: state.daytimeHeader,
                                    withArrow: true,
                                    rightView: AnyView(
                                            MyListView__ItemView__ButtonView__RightText(
                                                    text: state.daytimeNote,
                                                    paddingEnd: 2
                                            )
                                    )
                            ) {
                                hideKeyboard()
                                isDaytimeSheetPresented = true
                            }
                                    .sheetEnv(isPresented: $isDaytimeSheetPresented) {
                                        DaytimePickerSheet(
                                                isPresented: $isDaytimeSheetPresented,
                                                title: state.daytimeHeader,
                                                doneText: "Done",
                                                defMinute: state.daytimePickerDefMinute,
                                                defHour: state.daytimePickerDefHour
                                        ) { seconds in
                                            vm.upDaytime(newDaytimeOrNull: seconds?.toKotlinInt())
                                        }
                                                .presentationDetentsMediumIf16()
                                    }
                        }

                        MyListView__ItemView(
                                isFirst: false,
                                isLast: true,
                                withTopDivider: true
                        ) {
                            MyListView__ItemView__SwitchView(
                                    text: state.isImportantHeader,
                                    isActive: state.isImportant
                            ) {
                                vm.toggleIsImportant()
                            }
                        }
                    }

                    VStack {

                        MyListView__Padding__SectionHeader()

                        MyListView__HeaderView(title: "REPETITION PERIOD")

                        MyListView__Padding__HeaderSection()
                    }

                    ForEach(0..<state.periods.count, id: \.self) { periodIndex in

                        let isFirst = periodIndex == 0

                        MyListView__ItemView(
                                isFirst: isFirst,
                                isLast: state.periods.count == periodIndex + 1,
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
                                                size: 30,
                                                onChange: { newWeekDays in
                                                    vm.upWeekDays(newWeekDays: newWeekDays)
                                                }
                                        )
                                                .padding(.top, 4)
                                                .padding(.bottom, 16)
                                                .padding(.leading, MyListView.PADDING_INNER_HORIZONTAL - 1)
                                    } else if periodIndex == 3 {
                                        let dayNumbers: [Int] = Array(1..<(RepeatingModel.companion.MAX_DAY_OF_MONTH.toInt() + 1))
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
                                            let isDaySelected = state.selectedDaysOfMonth.contains(RepeatingModel.companion.LAST_DAY_OF_MONTH.toInt().toKotlinInt())
                                            DayOfMonthItemView(
                                                    text: "Last Day of the Month",
                                                    isDaySelected: isDaySelected,
                                                    width: .infinity,
                                                    hPaddings: 10
                                            ) {
                                                vm.toggleDayOfMonth(day: RepeatingModel.companion.LAST_DAY_OF_MONTH)
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
                            .frame(minHeight: 20)
                }
            }
                    .onChange(of: state.activePeriodIndex) { _ in
                        hideKeyboard()
                    }
        }
                .background(c.sheetBg)
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
    let onSelect: (RepeatingModelPeriodDaysOfYear.MonthDayItem) -> Void

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
                        RepeatingModelPeriodDaysOfYear.MonthDayItem(
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
                    ForEach(RepeatingModelPeriodDaysOfYear.companion.months, id: \.id) { month in
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
        let month = RepeatingModelPeriodDaysOfYear.companion.months.first {
            $0.id == selectedMonthId
        }!
        pickerDayIds = Array(month.days.first...month.days.last)
    }
}
