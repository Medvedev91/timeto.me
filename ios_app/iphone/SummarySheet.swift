import SwiftUI
import shared

private let bottomBarButtonFontSize = 22.0
private let bottomBarButtonFontWeight = Font.Weight.light
private let bottomBarButtonFontColor = c.textSecondary
private let bottomBarButtonFrameSize = 32.0

private let barsHeaderHeight = 36.0
private let hPadding = 8.0

struct SummarySheet: View {

    @Binding var isPresented: Bool

    ///

    @State private var vm = SummarySheetVm()

    var body: some View {

        VMView(vm: vm, stack: .VStack()) { state in

            ZStack {

                HStack {

                    //
                    // Left Part

                    ZStack {

                        //
                        // Bars Time Sheet

                        VStack {
                            ForEachIndexed(state.barsTimeRows) { _, barString in
                                VStack(alignment: .leading) {
                                    Spacer()
                                    Text(barString)
                                            .foregroundColor(c.textSecondary)
                                            .font(.system(size: 10, weight: .light))
                                            .padding(.bottom, 4)
                                    SheetDividerFg()
                                }
                                        .padding(.leading, hPadding)
                                        .padding(.trailing, 4)
                                Spacer()
                            }
                        }
                                .padding(.top, barsHeaderHeight)

                        //
                        // Bars

                        GeometryReader { geometry in

                            ScrollView(.horizontal) {

                                HStack {

                                    Spacer()

                                    ForEachIndexed(state.daysIntervalsUi.reversed()) { _, dayIntervalsUi in

                                        VStack {

                                            VStack {

                                                Spacer()

                                                Text(dayIntervalsUi.dayString)
                                                        .lineLimit(1)
                                                        .foregroundColor(c.textSecondary)
                                                        .font(.system(size: 10, weight: .light))
                                            }
                                                    .padding(.bottom, 8)
                                                    .frame(height: barsHeaderHeight)

                                            GeometryReader { geometry in
                                                VStack {
                                                    ForEachIndexed(dayIntervalsUi.intervalsUi) { _, intervalUi in
                                                        ZStack {}
                                                                .frame(minWidth: 0, maxWidth: .infinity)
                                                                .frame(height: CGFloat(intervalUi.ratio) * geometry.size.height)
                                                                .background(intervalUi.activityDb?.colorRgba.toColor() ?? c.sheetFg)
                                                    }
                                                }
                                                        .clipShape(roundedShape)
                                                        .padding(.horizontal, 4)
                                            }
                                        }
                                                .frame(width: 16)
                                    }
                                }
                                        .frame(minWidth: geometry.size.width)
                            }
                        }
                                .padding(.leading, 28)
                    }
                            .frame(minWidth: 0, maxWidth: .infinity)
                            .padding(.bottom, 12)
                            .padding(.trailing, 12)

                    //
                    // Right Part

                    ScrollView {

                        VStack {

                            ForEachIndexed(state.activitiesUI) { idx, activityUI in

                                let activityColor = activityUI.activity.colorRgba.toColor()

                                VStack {

                                    HStack {

                                        ActivitySecondaryText(text: activityUI.perDayString)

                                        Spacer()

                                        ActivitySecondaryText(text: activityUI.totalTimeString)
                                    }

                                    HStack {

                                        Text(activityUI.title)
                                                .padding(.trailing, 4)
                                                .foregroundColor(c.text)
                                                .font(.system(size: 14, weight: .medium))
                                                .lineLimit(1)

                                        Spacer()

                                        ActivitySecondaryText(text: activityUI.percentageString)
                                    }
                                            .padding(.top, 4)


                                    HStack {

                                        ZStack {

                                            GeometryReader { geometry in

                                                ZStack {}
                                                        .frame(maxHeight: .infinity)
                                                        .frame(width: geometry.size.width * Double(activityUI.ratio))
                                                        .background(activityColor)
                                            }
                                                    .frame(width: .infinity)
                                        }
                                                .frame(height: 8)
                                                .frame(minWidth: 0, maxWidth: .infinity)
                                                .background(c.sheetFg)
                                                .clipShape(roundedShape)

                                        Padding(horizontal: 4)

                                        ZStack {}
                                                .frame(width: 8, height: 8)
                                                .background(roundedShape.fill(activityColor))
                                    }
                                            .padding(.top, 6)
                                }
                                        .padding(.top, 16)
                                        .padding(.trailing, hPadding)
                            }

                            Padding(vertical: 12)
                        }
                                .frame(minWidth: 0, maxWidth: .infinity)
                    }
                }

                if state.isChartVisible {
                    SummaryChartView(activitiesUI: state.activitiesUI)
                            .id(state)
                }
            }

            Spacer()

            Sheet__BottomView {

                VStack {

                    HStack {

                        ForEachIndexed(state.periodHints) { _, period in

                            Button(
                                    action: {
                                        vm.setPeriod(
                                                pickerTimeStart: period.pickerTimeStart,
                                                pickerTimeFinish: period.pickerTimeFinish
                                        )
                                    },
                                    label: {
                                        Text(period.title)
                                                .font(.system(size: 14, weight: period.isActive ? .bold : .light))
                                                .foregroundColor(period.isActive ? c.white : c.text)
                                                .padding(.horizontal, 8)
                                                .padding(.vertical, 6)
                                    }
                            )
                        }
                    }
                            .padding(.top, 12)

                    HStack {

                        Button(
                                action: {
                                    vm.toggleIsChartVisible()
                                },
                                label: {
                                    HStack {
                                        Image(systemName: "chart.pie")
                                                .font(.system(
                                                        size: state.isChartVisible ? 20 : bottomBarButtonFontSize,
                                                        weight: bottomBarButtonFontWeight
                                                ))
                                                .foregroundColor(state.isChartVisible ? c.white : bottomBarButtonFontColor)
                                    }
                                            .frame(width: bottomBarButtonFrameSize, height: bottomBarButtonFrameSize)
                                            .background(roundedShape.fill(state.isChartVisible ? c.blue : c.transparent))
                                }
                        )

                        Spacer()

                        DatePickerStateView(
                                unixTime: state.pickerTimeStart,
                                minTime: state.minPickerTime,
                                maxTime: state.maxPickerTime
                        ) { newTime in
                            vm.setPickerTimeStart(unixTime: newTime)
                        }
                                .labelsHidden()

                        Text("-")
                                .padding(.horizontal, 6)

                        DatePickerStateView(
                                unixTime: state.pickerTimeFinish,
                                minTime: state.minPickerTime,
                                maxTime: state.maxPickerTime
                        ) { newTime in
                            vm.setPickerTimeFinish(unixTime: newTime)
                        }
                                .labelsHidden()

                        Spacer()

                        Button(
                                action: {
                                    isPresented = false
                                },
                                label: {
                                    HStack {
                                        Image(systemName: "xmark.circle")
                                                .font(.system(
                                                        size: bottomBarButtonFontSize,
                                                        weight: bottomBarButtonFontWeight
                                                ))
                                                .foregroundColor(bottomBarButtonFontColor)
                                    }
                                            .frame(width: bottomBarButtonFrameSize, height: bottomBarButtonFrameSize)
                                }
                        )
                    }
                            .padding(.top, 10)
                            .padding(.horizontal, 16)
                }
            }
        }
    }
}

private struct ActivitySecondaryText: View {

    let text: String

    var body: some View {
        Text(text)
                .font(.system(size: 12, weight: .light))
                .lineLimit(1)
                .foregroundColor(c.textSecondary)
    }
}
