import SwiftUI
import shared

private let bottomBarButtonFontSize = 22.0
private let bottomBarButtonFontWeight = Font.Weight.light
private let bottomBarButtonFontColor: Color = .secondary
private let bottomBarButtonFrameSize = 32.0

private let barsHeaderHeight = 36.0
private let hPadding = 8.0

struct SummarySheet: View {
    
    let onClose: () -> Void
    
    var body: some View {
        VmView({
            SummaryVm()
        }) { vm, state in
            SummarySheetInner(
                vm: vm,
                state: state,
                onClose: onClose
            )
        }
    }
}

private struct SummarySheetInner: View {
    
    let vm: SummaryVm
    let state: SummaryVm.State
    
    let onClose: () -> Void
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    
    @State private var isChartVisible: Bool = false
    
    var body: some View {
        
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
                                    .foregroundColor(.secondary)
                                    .font(.system(size: 10, weight: .light))
                                    .padding(.bottom, 4)
                                Divider()
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
                                
                                ForEachIndexed(state.daysBarsUi.reversed()) { _, dayBarsUi in
                                    
                                    VStack {
                                        
                                        VStack {
                                            
                                            Spacer()
                                            
                                            Text(dayBarsUi.dayString)
                                                .lineLimit(1)
                                                .foregroundColor(.secondary)
                                                .font(.system(size: 10, weight: .light))
                                        }
                                        .padding(.bottom, 8)
                                        .frame(height: barsHeaderHeight)
                                        
                                        GeometryReader { geometry in
                                            VStack {
                                                ForEachIndexed(dayBarsUi.barsUi) { _, barUi in
                                                    ZStack {}
                                                        .frame(minWidth: 0, maxWidth: .infinity)
                                                        .frame(height: CGFloat(barUi.ratio) * geometry.size.height)
                                                        .background(barUi.goalDb?.colorRgba.toColor() ?? Color(.systemGray5))
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
                        
                        ForEachIndexed(state.goalsUi) { idx, goalUi in
                            GoalView(goalUi: goalUi)
                        }
                        
                        Padding(vertical: 12)
                    }
                    .frame(minWidth: 0, maxWidth: .infinity)
                }
            }

            if isChartVisible {
                SummaryChartView(activitiesUi: state.goalsUi)
                    .id(state)
            }
        }
        
        Spacer()
        
        VStack {
            
            Divider()
            
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
                                .foregroundColor(period.isActive ? .white : .primary)
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
                        isChartVisible.toggle()
                    },
                    label: {
                        HStack {
                            Image(systemName: "chart.pie")
                                .font(.system(
                                    size: isChartVisible ? 20 : bottomBarButtonFontSize,
                                    weight: bottomBarButtonFontWeight
                                ))
                                .foregroundColor(isChartVisible ? .white : bottomBarButtonFontColor)
                        }
                        .frame(width: bottomBarButtonFrameSize, height: bottomBarButtonFrameSize)
                        .background(roundedShape.fill(isChartVisible ? .blue : .clear))
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
                        dismiss()
                        onClose()
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
        .background(.black)
    }
}

private struct ActivitySecondaryText: View {
    
    let text: String
    
    var body: some View {
        Text(text)
            .font(.system(size: 12, weight: .light))
            .lineLimit(1)
            .foregroundColor(.secondary)
    }
}

private struct GoalView: View {
    
    let goalUi: SummaryVm.GoalUi
    
    ///
    
    private var goalColor: Color {
        goalUi.goalDb.colorRgba.toColor()
    }
    
    var body: some View {
        
        VStack {
            
            HStack {
                
                ActivitySecondaryText(text: goalUi.perDayString)
                
                Spacer()
                
                ActivitySecondaryText(text: goalUi.totalTimeString)
            }
            
            HStack {
                
                Text(goalUi.title)
                    .padding(.trailing, 4)
                    .foregroundColor(.primary)
                    .font(.system(size: 14, weight: .medium))
                    .lineLimit(1)
                
                Spacer()
                
                ActivitySecondaryText(text: goalUi.percentageString)
            }
            .padding(.top, 4)
            
            
            HStack {
                
                ZStack {
                    
                    GeometryReader { geometry in
                        
                        ZStack {}
                            .frame(maxHeight: .infinity)
                            .frame(width: geometry.size.width * Double(goalUi.ratio))
                            .background(goalColor)
                    }
                    .fillMaxWidth()
                }
                .frame(height: 8)
                .frame(minWidth: 0, maxWidth: .infinity)
                .background(Color(.systemGray5))
                .clipShape(roundedShape)
                
                Padding(horizontal: 4)
                
                ZStack {}
                    .frame(width: 8, height: 8)
                    .background(roundedShape.fill(goalColor))
            }
            .padding(.top, 6)
        }
        .padding(.top, 16)
        .padding(.trailing, hPadding)
        
        if goalUi.children.count > 0 {
            HStack {
                
                VStack {
                    Spacer()
                }
                .frame(width: 2)
                .background(roundedShape.fill(goalColor))
                .padding(.top, 17)
                .offset(y: halfDpCeil)

                VStack {
                    ForEachIndexed(goalUi.children as! [SummaryVm.GoalUi]) { idx, childrenGoalUi in
                        GoalView(goalUi: childrenGoalUi)
                    }
                }
                .padding(.leading, 12)
            }
        }
    }
}
