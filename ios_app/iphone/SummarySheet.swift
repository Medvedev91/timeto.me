import SwiftUI
import shared

private let bottomBarButtonFontSize = 22.0
private let bottomBarButtonFontWeight = Font.Weight.light
private let bottomBarButtonFontColor = c.textSecondary
private let bottomBarButtonFrameSize = 32.0

struct SummarySheet: View {

    @Binding var isPresented: Bool

    ///

    @State private var vm = SummarySheetVM()

    var body: some View {

        VMView(vm: vm, stack: .VStack()) { state in

            if state.isChartVisible {
                ChartView(activitiesUI: state.activitiesUI)
                        .id(state)
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
