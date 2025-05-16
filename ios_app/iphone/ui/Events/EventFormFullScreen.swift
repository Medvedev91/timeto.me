import SwiftUI
import shared

struct EventFormFullScreen: View {
    
    let initEventDb: EventDb?
    let initText: String?
    let initTime: Int?
    
    var body: some View {
        VmView({
            EventFormVm(
                initEventDb: initEventDb,
                initText: initText,
                initTime: initTime?.toKotlinInt()
            )
        }) { vm, state in
            EventFormFullScreenInner(
                vm: vm,
                state: state,
                date: Date(timeIntervalSince1970: Double(state.selectedUnixTime.time)),
                text: state.text
            )
        }
    }
}

private struct EventFormFullScreenInner: View {
    
    let vm: EventFormVm
    let state: EventFormVm.State
    
    @State var date: Date
    @State var text: String
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    @FocusState private var isFocused: Bool
    
    var body: some View {
        
        VStack {
            
            ScrollView(showsIndicators: false) {
                
                VStack {
                    
                    HStack {
                        
                        DatePicker(
                            "Start Date",
                            selection: $date,
                            in: Date().startOfDay()...,
                            displayedComponents: [.date]
                        )
                        .labelsHidden()
                        .datePickerStyle(.compact)
                        .padding(.leading, H_PADDING)
                        
                        Spacer()
                    }
                    
                    DaytimePickerSliderView(
                        daytimeUi: state.daytimeUi,
                        onChange: { daytimeUi in
                            vm.setDaytime(daytimeUi: daytimeUi)
                        }
                    )
                    .padding(.top, 16)
                    .padding(.bottom, 12)
                    
                    EventTemplatesView(
                        onDone: { templateDb in
                            text = vm.setTemplate(eventTemplateDb: templateDb)
                        }
                    )
                    .padding(.bottom, 10)
                }
            }
            .defaultScrollAnchor(.bottom)
            
            HStack {
                
                TextField(
                    state.textPlaceholder,
                    text: $text,
                    axis: .vertical
                )
                .focused($isFocused)
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .onChange(of: text) { _, newText in
                    vm.setText(text: newText)
                }
                
                Button(
                    action: {
                        vm.save(
                            dialogsManager: navigation,
                            onSuccess: {
                                dismiss()
                            }
                        )
                    },
                    label: {
                        Text(state.saveText)
                            .font(.system(size: 16, weight: .semibold))
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .foregroundColor(.primary)
                            .background(roundedShape.fill(.blue))
                    }
                )
                .padding(.leading, 8)
                .padding(.trailing, H_PADDING)
            }
            .padding(.vertical, 4)
            .background(Color(.secondarySystemBackground))
        }
        .toolbarTitleDisplayMode(.inline)
        .toolbar {
            if let eventDb = vm.initEventDb {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Delete") {
                        vm.delete(
                            eventDb: eventDb,
                            dialogsManager: navigation,
                            onSuccess: {
                                dismiss()
                            }
                        )
                    }
                    .foregroundColor(.red)
                }
            }
            ToolbarItem(placement: .primaryAction) {
                Button("Cancel") {
                    dismiss()
                }
            }
        }
        .onChange(of: date) { _, newDate in
            vm.setUnixDay(unixDay: newDate.toUnixTime().localDay)
        }
        .onAppear {
            isFocused = true
        }
    }
}
