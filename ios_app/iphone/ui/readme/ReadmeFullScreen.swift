import SwiftUI
import shared

struct ReadmeFullScreen: View {
    
    let defaultItem: ReadmeVm.DefaultItem
    
    var body: some View {
        VmView({
            ReadmeVm(defaultItem: defaultItem)
        }) { vm, state in
            ReadmeFullScreenInner(
                vm: vm,
                state: state,
                selectedTab: state.tabUi
            )
        }
    }
}

private struct ReadmeFullScreenInner: View {
    
    let vm: ReadmeVm
    let state: ReadmeVm.State
    
    @State var selectedTab: ReadmeVm.TabUi
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    @State private var isScrolled: Bool = false
    
    var body: some View {
        
        ZStack {
            ReadmeTabView(tabUi: state.tabUi, isScrolled: $isScrolled)
                .id("tab_\(state.tabUi.id)") // To scroll to top
        }
        .safeAreaInset(edge: .top) {
            
            VStack {
                
                ZStack {
                    
                    HStack {
                        Spacer()
                        Button("Done") {
                            dismiss()
                        }
                        .fontWeight(.semibold)
                    }
                    .padding(.horizontal, 16)
                    
                    Text(state.title)
                        .fontWeight(.semibold)
                        .foregroundColor(.primary)
                }
                .padding(.top, 6)
                
                Picker("", selection: $selectedTab) {
                    ForEach(state.tabsUi, id: \.id) { tabUi in
                        Text(tabUi.title)
                            .tag(tabUi)
                    }
                }
                .pickerStyle(.segmented)
                .padding(.horizontal, 16)
                .padding(.top, 18)
                .padding(.bottom, 12)
                
                Color(isScrolled ? .systemGray5 : .clear).frame(height: onePx)
            }
            .background(isScrolled ? AnyShapeStyle(.bar) : AnyShapeStyle(.clear))
        }
        .navigationBarHidden(true)
        .statusBarHidden(true)
        .onChange(of: selectedTab) { _, new in
            vm.setTabUi(tabUi: new)
        }
    }
}
